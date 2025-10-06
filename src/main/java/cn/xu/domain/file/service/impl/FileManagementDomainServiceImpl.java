package cn.xu.domain.file.service.impl;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.file.model.aggregate.FileAggregate;
import cn.xu.domain.file.model.entity.FileEntity;
import cn.xu.domain.file.model.valobj.FileUrl;
import cn.xu.domain.file.repository.IFileRepository;
import cn.xu.domain.file.service.FileManagementDomainService;
import cn.xu.domain.file.service.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理领域服务实现
 * 处理文件管理的核心业务逻辑
 */
@Service
@Slf4j
public class FileManagementDomainServiceImpl implements FileManagementDomainService {
    
    @Resource
    private IFileStorageService fileStorageService;
    
    @Resource
    private IFileRepository fileRepository;
    
    @Override
    public FileAggregate uploadSingleFile(MultipartFile file, Long uploadUserId) {
        log.info("开始上传单个文件: {}, 用户ID: {}", file.getOriginalFilename(), uploadUserId);
        
        try {
            // 1. 创建文件聚合根
            FileAggregate fileAggregate = FileAggregate.createFromUpload(file, uploadUserId);
            
            // 2. 验证业务规则
            fileAggregate.validate();
            
            // 3. 执行文件上传
            String fileUrl = fileStorageService.uploadFile(file, fileAggregate.getFileEntity().getSystemName().getFullName());
            
            // 4. 设置文件URL
            FileUrl url = new FileUrl(fileUrl);
            fileAggregate.getFileEntity().setFileUrl(url);
            fileAggregate.getFileEntity().setStoragePath(url.getObjectName());
            
            // 5. 保存文件信息到数据库
            fileRepository.save(fileAggregate.getFileEntity());
            
            log.info("单个文件上传成功: {}", fileUrl);
            return fileAggregate;
            
        } catch (Exception e) {
            log.error("上传单个文件失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public FileAggregate uploadBatchFiles(MultipartFile[] files, Long uploadUserId) {
        log.info("开始批量上传文件: {} 个文件, 用户ID: {}", files.length, uploadUserId);
        
        try {
            // 1. 创建批量文件聚合根
            FileAggregate fileAggregate = FileAggregate.createFromBatchUpload(files, uploadUserId);
            
            // 2. 验证业务规则
            fileAggregate.validate();
            
            // 3. 执行批量上传
            List<String> uploadedUrls = fileStorageService.uploadFiles(files);
            
            // 4. 设置文件URL
            List<FileEntity> allFiles = fileAggregate.getAllFiles();
            for (int i = 0; i < allFiles.size() && i < uploadedUrls.size(); i++) {
                FileEntity fileEntity = allFiles.get(i);
                FileUrl url = new FileUrl(uploadedUrls.get(i));
                fileEntity.setFileUrl(url);
                fileEntity.setStoragePath(url.getObjectName());
            }
            
            // 5. 批量保存文件信息到数据库
            fileRepository.batchSave(fileAggregate.getAllFiles());
            
            log.info("批量文件上传成功: {} 个文件", uploadedUrls.size());
            return fileAggregate;
            
        } catch (Exception e) {
            log.error("批量上传文件失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteSingleFile(String fileUrl, Long operatorUserId) {
        log.info("开始删除单个文件: {}, 操作用户ID: {}", fileUrl, operatorUserId);
        
        try {
            // 1. 查询文件信息
            FileEntity fileEntity = fileRepository.findByUrl(fileUrl);
            if (fileEntity == null) {
                log.warn("文件不存在: {}", fileUrl);
                return;
            }
            
            // 2. 验证删除权限
            validateDeletePermission(fileEntity, operatorUserId);
            
            // 3. 执行文件删除
            fileStorageService.deleteFile(fileUrl);
            
            // 4. 更新数据库状态
            fileEntity.markAsDeleted();
            fileRepository.update(fileEntity);
            
            log.info("单个文件删除成功: {}", fileUrl);
            
        } catch (Exception e) {
            log.error("删除单个文件失败: {}", fileUrl, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件删除失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteBatchFiles(List<String> fileUrls, Long operatorUserId) {
        log.info("开始批量删除文件: {} 个文件, 操作用户ID: {}", fileUrls.size(), operatorUserId);
        
        List<String> failedUrls = new ArrayList<>();
        
        for (String fileUrl : fileUrls) {
            try {
                deleteSingleFile(fileUrl, operatorUserId);
            } catch (Exception e) {
                log.error("删除文件失败: {}", fileUrl, e);
                failedUrls.add(fileUrl);
            }
        }
        
        if (!failedUrls.isEmpty()) {
            log.warn("部分文件删除失败: {}", failedUrls);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), 
                String.format("部分文件删除失败，失败数量: %d", failedUrls.size()));
        }
        
        log.info("批量文件删除成功: {} 个文件", fileUrls.size());
    }
    
    @Override
    public String moveToFormalStorage(String tempFileUrl) {
        log.info("开始移动临时文件到正式存储: {}", tempFileUrl);
        
        try {
            // 1. 查询文件信息
            FileEntity fileEntity = fileRepository.findByUrl(tempFileUrl);
            if (fileEntity == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "临时文件不存在");
            }
            
            // 2. 验证文件状态
            if (fileEntity.getStatus() != FileEntity.FileStatus.TEMPORARY) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "只有临时文件可以移动到正式存储");
            }
            
            // 3. 执行文件移动
            String formalUrl = fileStorageService.moveToFormal(tempFileUrl);
            
            // 4. 更新文件状态
            fileEntity.moveToFormal();
            fileEntity.setFileUrl(new FileUrl(formalUrl));
            fileEntity.setStoragePath(fileEntity.getFileUrl().getObjectName());
            fileRepository.update(fileEntity);
            
            log.info("临时文件移动成功: {} -> {}", tempFileUrl, formalUrl);
            return formalUrl;
            
        } catch (Exception e) {
            log.error("移动临时文件失败: {}", tempFileUrl, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "移动文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> moveBatchToFormalStorage(List<String> tempFileUrls) {
        log.info("开始批量移动临时文件到正式存储: {} 个文件", tempFileUrls.size());
        
        List<String> formalUrls = new ArrayList<>();
        
        try {
            for (String tempUrl : tempFileUrls) {
                String formalUrl = moveToFormalStorage(tempUrl);
                formalUrls.add(formalUrl);
            }
            
            log.info("批量移动临时文件成功: {} 个文件", formalUrls.size());
            return formalUrls;
            
        } catch (Exception e) {
            log.error("批量移动临时文件失败", e);
            // 回滚已移动的文件
            if (!formalUrls.isEmpty()) {
                try {
                    fileStorageService.deleteFiles(formalUrls);
                } catch (Exception rollbackException) {
                    log.error("回滚文件失败", rollbackException);
                }
            }
            throw e;
        }
    }
    
    @Override
    public boolean validateFileAccess(String fileUrl, Long userId) {
        try {
            FileEntity fileEntity = fileRepository.findByUrl(fileUrl);
            if (fileEntity == null) {
                return false;
            }
            
            // 检查文件状态
            if (!fileEntity.canBeAccessed()) {
                return false;
            }
            
            // 检查用户权限（临时文件只能被上传者访问）
            if (fileEntity.getStatus() == FileEntity.FileStatus.TEMPORARY) {
                return fileEntity.getUploadUserId().equals(userId);
            }
            
            // 正式文件可以被所有用户访问
            return true;
            
        } catch (Exception e) {
            log.error("验证文件访问权限失败: {}", fileUrl, e);
            return false;
        }
    }
    
    @Override
    public int cleanupExpiredTemporaryFiles(int expirationHours) {
        log.info("开始清理过期临时文件，过期时间: {} 小时", expirationHours);
        
        try {
            LocalDateTime expirationTime = LocalDateTime.now().minusHours(expirationHours);
            List<FileEntity> expiredFiles = fileRepository.findExpiredTemporaryFiles(expirationTime);
            
            int cleanedCount = 0;
            for (FileEntity fileEntity : expiredFiles) {
                try {
                    if (fileEntity.getFileUrl() != null) {
                        fileStorageService.deleteFile(fileEntity.getFileUrl().getUrl());
                    }
                    fileEntity.markAsDeleted();
                    fileRepository.update(fileEntity);
                    cleanedCount++;
                } catch (Exception e) {
                    log.error("清理过期文件失败: {}", fileEntity.getFileId(), e);
                }
            }
            
            log.info("清理过期临时文件完成，清理数量: {}", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            log.error("清理过期临时文件失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "清理过期文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证删除权限
     */
    private void validateDeletePermission(FileEntity fileEntity, Long operatorUserId) {
        if (!fileEntity.getUploadUserId().equals(operatorUserId)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无权限删除他人上传的文件");
        }
        
        if (!fileEntity.canBeDeleted()) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件已删除，无法重复删除");
        }
    }
}