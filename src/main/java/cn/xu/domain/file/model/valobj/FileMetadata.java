package cn.xu.domain.file.model.valobj;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 文件元数据值对象
 * 封装文件的大小、类型等元信息
 */
@Getter
public class FileMetadata {
    
    /**
     * 文件大小（字节）
     */
    private final long size;
    
    /**
     * 文件MIME类型
     */
    private final String contentType;
    
    /**
     * 文件大小限制（5MB）
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 允许的MIME类型
     */
    private static final String[] ALLOWED_CONTENT_TYPES = {
        "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
        "application/pdf", "text/plain", "text/markdown",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };
    
    /**
     * 构造函数
     * @param size 文件大小
     * @param contentType MIME类型
     */
    public FileMetadata(long size, String contentType) {
        this.size = size;
        this.contentType = StringUtils.isBlank(contentType) ? "application/octet-stream" : contentType.trim();
        
        validateSize();
        validateContentType();
    }
    
    /**
     * 验证文件大小
     */
    private void validateSize() {
        if (size <= 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件大小必须大于0");
        }
        
        if (size > MAX_FILE_SIZE) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
                String.format("文件大小不能超过%dMB", MAX_FILE_SIZE / (1024 * 1024)));
        }
    }
    
    /**
     * 验证MIME类型
     */
    private void validateContentType() {
        boolean isAllowed = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
                "不支持的文件类型: " + contentType);
        }
    }
    
    /**
     * 判断是否为图片文件
     */
    public boolean isImage() {
        return contentType.startsWith("image/");
    }
    
    /**
     * 判断是否为文档文件
     */
    public boolean isDocument() {
        return contentType.equals("application/pdf") || 
               contentType.equals("text/plain") || 
               contentType.equals("text/markdown") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
    
    /**
     * 获取格式化的文件大小
     */
    public String getFormattedSize() {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1fKB", size / 1024.0);
        } else {
            return String.format("%.1fMB", size / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 判断是否为大文件（超过1MB）
     */
    public boolean isLargeFile() {
        return size > 1024 * 1024;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return size == that.size && Objects.equals(contentType, that.contentType);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(size, contentType);
    }
    
    @Override
    public String toString() {
        return String.format("FileMetadata{size=%s, contentType='%s'}", getFormattedSize(), contentType);
    }
}