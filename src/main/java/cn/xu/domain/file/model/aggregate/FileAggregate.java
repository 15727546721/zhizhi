package cn.xu.domain.file.model.aggregate;

import cn.xu.domain.file.model.entity.FileEntity;
import cn.xu.domain.file.model.valobj.FileName;
import cn.xu.domain.file.model.valobj.FileUrl;
import cn.xu.domain.file.model.valobj.FileMetadata;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件聚合根
 * 管理文件的完整生命周期和业务规则一致性
 */
@Data
public class FileAggregate {
    
    /**
     * 文件实体（聚合根）
     */
    private FileEntity fileEntity;
    
    /**
     * 关联的文件列表（批量上传场景）
     */
    private List<FileEntity> relatedFiles;
    
    /**
     * 聚合版本号（乐观锁）
     */
    private Long version;
    
    /**
     * 私有构造函数
     */
    private FileAggregate() {
        this.relatedFiles = new ArrayList<>();
        this.version = 0L;
    }
    
    /**
     * 创建单文件聚合
     * @param multipartFile 上传的文件
     * @param uploadUserId 上传用户ID
     * @return 文件聚合根
     */
    public static FileAggregate createFromUpload(MultipartFile multipartFile, Long uploadUserId) {
        FileAggregate aggregate = new FileAggregate();
        
        // 创建文件名值对象
        FileName originalName = new FileName(multipartFile.getOriginalFilename());
        
        // 创建文件元数据值对象
        FileMetadata metadata = new FileMetadata(multipartFile.getSize(), multipartFile.getContentType());
        
        // 创建文件实体
        FileEntity fileEntity = FileEntity.createTemporary(originalName, metadata, uploadUserId);
        
        aggregate.fileEntity = fileEntity;
        aggregate.version = 1L;
        
        return aggregate;
    }
    
    /**
     * 创建批量文件聚合
     * @param multipartFiles 上传的文件数组
     * @param uploadUserId 上传用户ID
     * @return 文件聚合根
     */
    public static FileAggregate createFromBatchUpload(MultipartFile[] multipartFiles, Long uploadUserId) {
        FileAggregate aggregate = new FileAggregate();
        
        for (MultipartFile file : multipartFiles) {
            FileName originalName = new FileName(file.getOriginalFilename());
            FileMetadata metadata = new FileMetadata(file.getSize(), file.getContentType());
            FileEntity fileEntity = FileEntity.createTemporary(originalName, metadata, uploadUserId);
            aggregate.relatedFiles.add(fileEntity);
        }
        
        aggregate.version = 1L;
        return aggregate;
    }
    
    /**
     * 设置文件URL（上传成功后）
     * @param baseUrl 基础URL
     * @param bucketName 存储桶名称
     */
    public void setFileUrl(String baseUrl, String bucketName) {
        if (fileEntity != null) {
            String objectName = generateObjectName(fileEntity);
            FileUrl fileUrl = new FileUrl(baseUrl, bucketName, objectName);
            fileEntity.setFileUrl(fileUrl);
            fileEntity.setStoragePath(objectName);
        }
        
        for (FileEntity entity : relatedFiles) {
            String objectName = generateObjectName(entity);
            FileUrl fileUrl = new FileUrl(baseUrl, bucketName, objectName);
            entity.setFileUrl(fileUrl);
            entity.setStoragePath(objectName);
        }
        
        incrementVersion();
    }
    
    /**
     * 移动到正式存储
     */
    public void moveToFormal() {
        if (fileEntity != null) {
            fileEntity.moveToFormal();
            updateFormalUrl();
        }
        
        for (FileEntity entity : relatedFiles) {
            entity.moveToFormal();
            updateFormalUrl(entity);
        }
        
        incrementVersion();
    }
    
    /**
     * 标记为已删除
     */
    public void markAsDeleted() {
        if (fileEntity != null) {
            fileEntity.markAsDeleted();
        }
        
        for (FileEntity entity : relatedFiles) {
            entity.markAsDeleted();
        }
        
        incrementVersion();
    }
    
    /**
     * 生成对象名称（存储路径）
     */
    private String generateObjectName(FileEntity entity) {
        String prefix = entity.getStatus() == FileEntity.FileStatus.TEMPORARY ? "temp" : "formal";
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return String.format("%s/%s/%s", prefix, dateFolder, entity.getSystemName().getFullName());
    }
    
    /**
     * 更新主文件的正式URL
     */
    private void updateFormalUrl() {
        if (fileEntity != null && fileEntity.getFileUrl() != null) {
            String currentUrl = fileEntity.getFileUrl().getUrl();
            String formalUrl = currentUrl.replace("/temp/", "/formal/");
            fileEntity.setFileUrl(new FileUrl(formalUrl));
            fileEntity.setStoragePath(fileEntity.getFileUrl().getObjectName());
        }
    }
    
    /**
     * 更新指定文件的正式URL
     */
    private void updateFormalUrl(FileEntity entity) {
        if (entity.getFileUrl() != null) {
            String currentUrl = entity.getFileUrl().getUrl();
            String formalUrl = currentUrl.replace("/temp/", "/formal/");
            entity.setFileUrl(new FileUrl(formalUrl));
            entity.setStoragePath(entity.getFileUrl().getObjectName());
        }
    }
    
    /**
     * 验证聚合的业务规则
     */
    public void validate() {
        if (fileEntity == null && relatedFiles.isEmpty()) {
            throw new IllegalStateException("文件聚合必须包含至少一个文件");
        }
        
        // 验证文件数量限制（最多10个文件）
        int totalFiles = (fileEntity != null ? 1 : 0) + relatedFiles.size();
        if (totalFiles > 10) {
            throw new IllegalStateException("单次上传文件数量不能超过10个");
        }
        
        // 验证总文件大小（最多50MB）
        long totalSize = 0;
        if (fileEntity != null) {
            totalSize += fileEntity.getMetadata().getSize();
        }
        for (FileEntity entity : relatedFiles) {
            totalSize += entity.getMetadata().getSize();
        }
        
        if (totalSize > 50 * 1024 * 1024) {
            throw new IllegalStateException("总文件大小不能超过50MB");
        }
    }
    
    /**
     * 获取所有文件实体
     */
    public List<FileEntity> getAllFiles() {
        List<FileEntity> allFiles = new ArrayList<>();
        if (fileEntity != null) {
            allFiles.add(fileEntity);
        }
        allFiles.addAll(relatedFiles);
        return allFiles;
    }
    
    /**
     * 获取所有文件URL
     */
    public List<String> getAllFileUrls() {
        List<String> urls = new ArrayList<>();
        for (FileEntity entity : getAllFiles()) {
            if (entity.getFileUrl() != null) {
                urls.add(entity.getFileUrl().getUrl());
            }
        }
        return urls;
    }
    
    /**
     * 检查是否包含图片文件
     */
    public boolean containsImages() {
        return getAllFiles().stream()
            .anyMatch(file -> file.getMetadata().isImage());
    }
    
    /**
     * 检查是否包含文档文件
     */
    public boolean containsDocuments() {
        return getAllFiles().stream()
            .anyMatch(file -> file.getMetadata().isDocument());
    }
    
    /**
     * 增加版本号
     */
    private void incrementVersion() {
        this.version++;
    }
    
    /**
     * 获取聚合标识
     */
    public String getAggregateId() {
        if (fileEntity != null) {
            return fileEntity.getFileId();
        }
        return relatedFiles.isEmpty() ? null : relatedFiles.get(0).getFileId() + "_batch";
    }
}