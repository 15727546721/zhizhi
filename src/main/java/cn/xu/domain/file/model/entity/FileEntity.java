package cn.xu.domain.file.model.entity;

import cn.xu.domain.file.model.valobj.FileName;
import cn.xu.domain.file.model.valobj.FileUrl;
import cn.xu.domain.file.model.valobj.FileMetadata;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件实体
 * 作为文件领域的聚合根，管理文件的生命周期和业务规则
 */
@Data
public class FileEntity {
    
    /**
     * 文件唯一标识
     */
    private String fileId;
    
    /**
     * 原始文件名（值对象）
     */
    private FileName originalName;
    
    /**
     * 系统生成的文件名（值对象）
     */
    private FileName systemName;
    
    /**
     * 文件访问URL（值对象）
     */
    private FileUrl fileUrl;
    
    /**
     * 文件元数据（值对象）
     */
    private FileMetadata metadata;
    
    /**
     * 文件存储路径
     */
    private String storagePath;
    
    /**
     * 文件状态
     */
    private FileStatus status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 上传用户ID
     */
    private Long uploadUserId;
    
    /**
     * 文件状态枚举
     */
    public enum FileStatus {
        TEMPORARY,  // 临时文件
        FORMAL,     // 正式文件
        DELETED     // 已删除
    }
    
    /**
     * 构造函数 - 创建临时文件
     */
    public static FileEntity createTemporary(FileName originalName, FileMetadata metadata, Long uploadUserId) {
        FileEntity file = new FileEntity();
        file.fileId = generateFileId();
        file.originalName = originalName;
        file.systemName = FileName.generateSystemName(originalName.getExtension());
        file.metadata = metadata;
        file.status = FileStatus.TEMPORARY;
        file.uploadUserId = uploadUserId;
        file.createTime = LocalDateTime.now();
        file.updateTime = LocalDateTime.now();
        return file;
    }
    
    /**
     * 移动到正式存储
     */
    public void moveToFormal() {
        if (this.status != FileStatus.TEMPORARY) {
            throw new IllegalStateException("只有临时文件可以移动到正式存储");
        }
        this.status = FileStatus.FORMAL;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 标记为已删除
     */
    public void markAsDeleted() {
        if (this.status == FileStatus.DELETED) {
            throw new IllegalStateException("文件已删除");
        }
        this.status = FileStatus.DELETED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 生成唯一文件ID
     */
    private static String generateFileId() {
        return System.nanoTime() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 验证文件是否可以删除
     */
    public boolean canBeDeleted() {
        return this.status != FileStatus.DELETED;
    }
    
    /**
     * 验证文件是否可以访问
     */
    public boolean canBeAccessed() {
        return this.status == FileStatus.FORMAL || this.status == FileStatus.TEMPORARY;
    }
}