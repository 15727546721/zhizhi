package cn.xu.domain.file.model.valobj;

import cn.xu.application.common.ResponseCode;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * 文件名值对象
 * 封装文件名相关的业务规则和验证逻辑
 */
@Getter
public class FileName {
    
    /**
     * Base62 字符集
     */
    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    /**
     * 允许的文件扩展名
     */
    private static final String[] ALLOWED_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp",  // 图片格式
        ".pdf", ".doc", ".docx", ".txt", ".md"              // 文档格式
    };
    
    /**
     * 文件名（不包含扩展名）
     */
    private final String name;
    
    /**
     * 文件扩展名
     */
    private final String extension;
    
    /**
     * 完整文件名
     */
    private final String fullName;
    
    /**
     * 构造函数
     * @param fullName 完整文件名
     */
    public FileName(String fullName) {
        if (StringUtils.isBlank(fullName)) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件名不能为空");
        }
        
        this.fullName = fullName.trim();
        
        // 解析文件名和扩展名
        int lastDotIndex = this.fullName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            this.name = this.fullName;
            this.extension = "";
        } else {
            this.name = this.fullName.substring(0, lastDotIndex);
            this.extension = this.fullName.substring(lastDotIndex).toLowerCase();
        }
        
        // 验证文件名
        validateFileName();
        validateExtension();
    }
    
    /**
     * 生成系统文件名
     * @param extension 文件扩展名
     * @return 系统生成的文件名
     */
    public static FileName generateSystemName(String extension) {
        String systemName = generateUniqueFileName() + extension;
        return new FileName(systemName);
    }
    
    /**
     * 验证文件名
     */
    private void validateFileName() {
        if (StringUtils.isBlank(name)) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件名不能为空");
        }
        
        if (name.length() > 100) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件名长度不能超过100个字符");
        }
        
        // 检查是否包含非法字符
        if (name.matches(".*[<>:\"/\\\\|?*].*")) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件名包含非法字符");
        }
    }
    
    /**
     * 验证文件扩展名
     */
    private void validateExtension() {
        if (StringUtils.isBlank(extension)) {
            return; // 允许没有扩展名的文件
        }
        
        boolean isAllowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), 
                "不支持的文件格式: " + extension);
        }
    }
    
    /**
     * 生成唯一文件名（16位Base62编码）
     */
    private static String generateUniqueFileName() {
        // 组合时间戳和UUID，确保高唯一性
        long timestamp = System.nanoTime();
        UUID uuid = UUID.randomUUID();
        long combined = timestamp ^ uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
        
        String base62FileName = base62Encode(Math.abs(combined));
        
        // 确保精确16位长度
        int targetLength = 16;
        if (base62FileName.length() < targetLength) {
            // 左侧填充随机字符到16位
            int paddingLength = targetLength - base62FileName.length();
            StringBuilder padded = new StringBuilder();
            for (int i = 0; i < paddingLength; i++) {
                padded.append(BASE62_ALPHABET.charAt(
                    (int)(System.nanoTime() % 62)
                ));
            }
            base62FileName = padded.toString() + base62FileName;
        } else if (base62FileName.length() > targetLength) {
            // 取后16位
            base62FileName = base62FileName.substring(base62FileName.length() - targetLength);
        }
        
        return base62FileName;
    }
    
    /**
     * Base62编码
     */
    private static String base62Encode(long num) {
        if (num == 0) return "0";
        
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(BASE62_ALPHABET.charAt((int)(num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }
    
    /**
     * 是否为图片文件
     */
    public boolean isImage() {
        return extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)");
    }
    
    /**
     * 是否为文档文件
     */
    public boolean isDocument() {
        return extension.matches("\\.(pdf|doc|docx|txt|md)");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileName fileName = (FileName) o;
        return Objects.equals(fullName, fileName.fullName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(fullName);
    }
    
    @Override
    public String toString() {
        return fullName;
    }
}