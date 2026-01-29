package cn.xu.common.constants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储路径工具
 * <p>统一生成安全的文件存储路径，不暴露业务类型</p>
 * 
 * <pre>
 * 存储结构：
 * MinIO Bucket: zhizhi/
 * └── yyyyMM/uuid.ext   - 所有文件统一格式
 * 
 * 示例: 202512/a1b2c3d4e5f6.jpg
 * </pre>
 */
public final class FilePathConstants {
    
    private FilePathConstants() {
    }
    
    private static final DateTimeFormatter DATE_FORMATTER = TimeConstants.YEAR_MONTH_FORMATTER;
    
    /**
     * 生成安全的文件路径
     * @param originalFileName 原始文件名
     * @return 格式: yyyyMM/uuid.ext
     */
    public static String buildPath(String originalFileName) {
        String datePath = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ext = getExtension(originalFileName);
        return datePath + "/" + uuid + ext;
    }
    
    /**
     * 获取文件扩展名
     */
    private static String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    }
}
