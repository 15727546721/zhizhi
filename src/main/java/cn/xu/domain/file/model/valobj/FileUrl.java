package cn.xu.domain.file.model.valobj;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 文件URL值对象
 * 封装文件访问URL相关的业务规则
 */
@Getter
public class FileUrl {
    
    /**
     * 完整的文件访问URL
     */
    private final String url;
    
    /**
     * 存储桶名称
     */
    private final String bucketName;
    
    /**
     * 对象名称（文件路径）
     */
    private final String objectName;
    
    /**
     * 构造函数
     * @param url 完整的文件URL
     */
    public FileUrl(String url) {
        if (StringUtils.isBlank(url)) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件URL不能为空");
        }
        
        this.url = url.trim();
        
        // 解析URL获取桶名和对象名
        try {
            // 假设URL格式为: http://domain/bucketName/objectName
            String[] parts = this.url.split("/");
            if (parts.length < 4) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件URL格式错误");
            }
            
            this.bucketName = parts[parts.length - 2];
            this.objectName = parts[parts.length - 1];
            
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件URL解析失败");
        }
        
        validateUrl();
    }
    
    /**
     * 构造函数
     * @param baseUrl 基础URL
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    public FileUrl(String baseUrl, String bucketName, String objectName) {
        if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(bucketName) || StringUtils.isBlank(objectName)) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "URL构建参数不能为空");
        }
        
        this.bucketName = bucketName.trim();
        this.objectName = objectName.trim();
        this.url = String.format("%s/%s/%s", baseUrl.trim(), this.bucketName, this.objectName);
        
        validateUrl();
    }
    
    /**
     * 验证URL格式
     */
    private void validateUrl() {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件URL必须以http://或https://开头");
        }
        
        if (url.length() > 500) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件URL长度不能超过500个字符");
        }
    }
    
    /**
     * 判断是否为临时文件URL
     */
    public boolean isTemporary() {
        return objectName.contains("/temp/");
    }
    
    /**
     * 判断是否为正式文件URL
     */
    public boolean isFormal() {
        return objectName.contains("/formal/");
    }
    
    /**
     * 获取文件名（不包含路径）
     */
    public String getFileName() {
        return objectName.substring(objectName.lastIndexOf("/") + 1);
    }
    
    /**
     * 获取文件路径（不包含文件名）
     */
    public String getFilePath() {
        int lastSlashIndex = objectName.lastIndexOf("/");
        return lastSlashIndex > 0 ? objectName.substring(0, lastSlashIndex) : "";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileUrl fileUrl = (FileUrl) o;
        return Objects.equals(url, fileUrl.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
    
    @Override
    public String toString() {
        return url;
    }
}