package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件记录实体
 *
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileRecord {
    
    /** 主键ID */
    private Long id;
    
    /** 存储名称（MinIO中的UUID名称，如：abc123.jpg） */
    private String fileName;
    
    /** 原始名称（用户上传时的文件名，如：我的照片.jpg） */
    private String originalFileName;
    
    /** 存储路径（相对路径，如：zhizhi/abc123.jpg） */
    private String storagePath;
    
    /** 存储桶名称（MinIO/OSS的bucket） */
    private String bucketName;
    
    /** 文件大小（字节） */
    private Long fileSize;
    
    /** MIME类型（如：image/jpeg, application/pdf） */
    private String contentType;
    
    /** 扩展名（如：jpg, png, pdf） */
    private String extension;
    
    /** 上传用户ID */
    private Long uploadUserId;
    
    /** 文件状态：0-临时文件（24h后清理）, 1-正式文件, 2-已删除 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 标记为正式文件
     * 当文件被业务对象引用后调用
     */
    public void markAsPermanent() {
        this.status = 1;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 标记为已删除
     * 软删除，实际文件仍在MinIO中
     */
    public void markAsDeleted() {
        this.status = 2;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 检查是否为临时文件
     * @return true-临时文件，false-正式或已删除
     */
    public boolean isTemporary() {
        return this.status != null && this.status == 0;
    }
    
    /**
     * 检查是否为正式文件
     */
    public boolean isPermanent() {
        return this.status != null && this.status == 1;
    }
    
    /**
     * 检查是否已删除
     */
    public boolean isDeleted() {
        return this.status != null && this.status == 2;
    }
    
    /**
     * 获取可读的文件大小
     */
    public String getReadableSize() {
        if (fileSize == null) return "0 B";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
