package cn.xu.service.file;

import cn.xu.integration.file.service.FileStorageService;
import cn.xu.integration.file.service.FileUrlService;
import cn.xu.model.entity.FileRecord;
import cn.xu.repository.impl.FileRecordRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理服务
 *
 * @author xu
 */
@Service
@RequiredArgsConstructor
public class FileManagementService {
    
    private static final Logger log = LoggerFactory.getLogger(FileManagementService.class);
    
    private final FileStorageService fileStorageService;
    private final FileRecordRepository fileRecordRepository;
    private final FileUrlService fileUrlService;
    
    /**
     * 上传单个文件
     * 
     * @param file 文件
     * @param userId 上传用户ID
     * @return 文件URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String uploadFile(MultipartFile file, Long userId) {
        log.info(">>>>>> FileManagementService.uploadFile 开始 <<<<<<");
        log.info("用户ID: {}, 文件名: {}", userId, file.getOriginalFilename());
        
        try {
            // 1. 上传文件到MinIO，获取文件名
            log.info("调用 fileStorageService.uploadFile...");
            String fileName = fileStorageService.uploadFile(file, file.getOriginalFilename());
            log.info("fileStorageService 返回的文件名: {}", fileName);
            
            if (fileName == null || fileName.trim().isEmpty()) {
                log.error("❌ fileStorageService 返回的文件名为空！");
                throw new BusinessException("文件上传失败：返回的文件名为空");
            }
            
            // 2. 构建存储路径（相对路径）
            String storagePath = fileUrlService.buildStoragePath("zhizhi", fileName);
            log.info("构建存储路径: {}", storagePath);
            
            // 3. 创建文件记录（存储相对路径）
            FileRecord record = buildFileRecord(file, fileName, storagePath, userId);
            fileRecordRepository.save(record);
            
            // 4. 动态生成访问URL
            String accessUrl = fileUrlService.generateAccessUrl(storagePath);
            
            log.info("✅ 文件上传成功 - 用户ID: {}, 存储路径: {}, 访问URL: {}", userId, storagePath, accessUrl);
            log.info(">>>>>> FileManagementService.uploadFile 完成 <<<<<<");
            
            return accessUrl;
            
        } catch (Exception e) {
            log.error("❌ 文件上传失败 - 用户ID: {}", userId, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传文件
     * 
     * @param files 文件数组
     * @param userId 上传用户ID
     * @return 文件URL列表
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> uploadFiles(MultipartFile[] files, Long userId) {
        if (files == null || files.length == 0) {
            throw new BusinessException("文件列表不能为空");
        }
        
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String url = uploadFile(file, userId);
                urls.add(url);
            } catch (Exception e) {
                log.error("批量上传中文件上传失败: {}", file.getOriginalFilename(), e);
                // 继续上传其他文件
            }
        }
        
        return urls;
    }
    
    /**
     * 标记文件为正式文件
     * 当文件被业务对象引用后调用此方法
     * 
     * @param urlOrPath 文件URL或存储路径
     */
    @Transactional(rollbackFor = Exception.class)
    public void markFileAsPermanent(String urlOrPath) {
        try {
            // 提取存储路径（兼容完URL和相对路径）
            String storagePath = fileUrlService.extractStoragePath(urlOrPath);
            
            // 查询文件记录
            FileRecord record = fileRecordRepository.findByStoragePath(storagePath);
            if (record != null && record.isTemporary()) {
                record.markAsPermanent();
                fileRecordRepository.update(record);
                log.info("文件已标记为正式 - 存储路径: {}", storagePath);
            }
        } catch (Exception e) {
            log.error("标记文件失败: {}", urlOrPath, e);
            throw new BusinessException("标记文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量标记文件为正式
     * 
     * @param fileUrls 文件URL列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void markFilesAsPermanent(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        for (String fileUrl : fileUrls) {
            try {
                markFileAsPermanent(fileUrl);
            } catch (Exception e) {
                log.warn("标记文件失败: {}", fileUrl, e);
            }
        }
    }
    
    /**
     * 批量删除文件（系统内部调用，无权限校验）
     * 用于业务对象删除时清理关联文件
     * 
     * @param fileUrls 文件URL列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        for (String fileUrl : fileUrls) {
            try {
                deleteFileInternal(fileUrl);
            } catch (Exception e) {
                log.warn("删除文件失败: {}", fileUrl, e);
            }
        }
    }
    
    /**
     * 内部删除文件方法（无权限校验）
     */
    private void deleteFileInternal(String urlOrPath) {
        try {
            String storagePath = fileUrlService.extractStoragePath(urlOrPath);
            FileRecord record = fileRecordRepository.findByStoragePath(storagePath);
            
            // 删除存储中的文件
            String fullUrl = fileUrlService.generateAccessUrl(storagePath);
            fileStorageService.deleteFile(fullUrl);
            
            // 更新数据库记录状态
            if (record != null) {
                record.markAsDeleted();
                fileRecordRepository.update(record);
            }
            
            log.debug("文件删除成功 - 存储路径: {}", storagePath);
        } catch (Exception e) {
            log.warn("删除文件失败: {}", urlOrPath, e);
        }
    }
    
    /**
     * 删除文件
     * 同时删除存储和数据库记录
     * 
     * @param fileUrl 文件URL
     * @param userId 操作用户ID（用于权限校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFile(String urlOrPath, Long userId) {
        try {
            // 提取存储路径
            String storagePath = fileUrlService.extractStoragePath(urlOrPath);
            
            // 1. 查询和校验权限
            FileRecord record = fileRecordRepository.findByStoragePath(storagePath);
            if (record != null && !record.getUploadUserId().equals(userId)) {
                throw new BusinessException("无权限删除该文件");
            }
            
            // 2. 删除存储中的文件（使用完整URL）
            String fullUrl = fileUrlService.generateAccessUrl(storagePath);
            fileStorageService.deleteFile(fullUrl);
            
            // 3. 更新数据库记录状态
            if (record != null) {
                record.markAsDeleted();
                fileRecordRepository.update(record);
            }
            
            log.info("文件删除成功 - 存储路径: {}, 用户ID: {}", storagePath, userId);
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", urlOrPath, e);
            throw new BusinessException("文件删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理临时文件
     * 定时任务调用，清理超过指定时间的临时文件
     * 
     * @param hours 超过多少小时的临时文件需要清理
     * @return 清理的文件数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanupTemporaryFiles(int hours) {
        try {
            // 查询临时文件
            List<FileRecord> tempFiles = fileRecordRepository.findTemporaryFilesBefore(hours);
            
            int cleanedCount = 0;
            for (FileRecord record : tempFiles) {
                try {
                    // 生成完整URL进行删除
                    String fullUrl = fileUrlService.generateAccessUrl(record.getStoragePath());
                    fileStorageService.deleteFile(fullUrl);
                    record.markAsDeleted();
                    fileRecordRepository.update(record);
                    cleanedCount++;
                } catch (Exception e) {
                    log.error("清理临时文件失败 - 存储路径: {}", record.getStoragePath(), e);
                }
            }
            
            log.info("临时文件清理完成 - 清理数量: {}", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("清理临时文件失败", e);
            return 0;
        }
    }
    
    /**
     * 获取用户上传的文件列表
     * 
     * @param userId 用户ID
     * @return 文件记录列表
     */
    public List<FileRecord> getUserFiles(Long userId) {
        try {
            return fileRecordRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("获取用户文件列表失败 - 用户ID: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取用户上传的文件列表（按状态筛选）
     * 
     * @param userId 用户ID
     * @param status 文件状态（0-临时, 1-正式, 2-删除）
     * @return 文件记录列表
     */
    public List<FileRecord> getUserFilesByStatus(Long userId, Integer status) {
        try {
            return fileRecordRepository.findByUserIdAndStatus(userId, status);
        } catch (Exception e) {
            log.error("获取用户文件列表失败 - 用户ID: {}, 状态: {}", userId, status, e);
            return new ArrayList<>();
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 构建文件记录
     */
    /**
     * 构建文件记录（V2.0 - 存储相对路径）
     * 注意：业务表（post, user等）通过存储路径关联文件
     */
    private FileRecord buildFileRecord(MultipartFile file, String fileName, String storagePath, Long userId) {
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        
        return FileRecord.builder()
                .fileName(fileName)
                .originalFileName(originalFileName)
                .storagePath(storagePath)  // 相对路径：zhizhi/abc123.jpg
                .bucketName("zhizhi")
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .extension(extension)
                .uploadUserId(userId)
                .status(0)  // 默认为临时文件，24小时后清理
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}
