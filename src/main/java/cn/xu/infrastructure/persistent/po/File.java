package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File implements Serializable {
    
    /**
     * 文件唯一标识
     */
    private String fileId;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 系统生成的文件名
     */
    private String systemName;
    
    /**
     * 文件访问URL
     */
    private String fileUrl;
    
    /**
     * 文件存储路径
     */
    private String storagePath;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型/MIME类型
     */
    private String mimeType;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 文件状态（TEMPORARY, FORMAL, DELETED）
     */
    private String status;
    
    /**
     * 上传用户ID
     */
    private Long uploadUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}