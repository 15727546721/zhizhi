package cn.xu.integration.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 文件URL生成服务
 * 
 * 设计目标：
 * 1. 解耦存储路径和访问URL
 * 2. 支持多环境配置
 * 3. 支持MinIO迁移
 * 4. 支持CDN切换
 * 
 * @author zhizhi
 */
@Service
@Slf4j
public class FileUrlService {
    
    /**
     * 文件访问模式
     * - direct: 直接访问MinIO（开发环境）
     * - proxy: 通过代理访问（生产环境）
     * - cdn: 通过CDN访问（生产环境）
     */
    @Value("${file.access.mode:direct}")
    private String accessMode;
    
    /**
     * MinIO 基础URL
     */
    @Value("${minio.url}")
    private String minioBaseUrl;
    
    /**
     * 文件代理服务URL（可选）
     */
    @Value("${file.proxy.base-url:}")
    private String proxyBaseUrl;
    
    /**
     * CDN URL（可选）
     */
    @Value("${file.cdn.base-url:}")
    private String cdnBaseUrl;
    
    /**
     * 根据存储路径生成访问URL
     * 
     * @param storagePath 存储路径（如：zhizhi/abc123.jpg）
     * @return 完整的访问URL
     */
    public String generateAccessUrl(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) {
            return null;
        }
        
        // 确保路径不以 / 开头
        if (storagePath.startsWith("/")) {
            storagePath = storagePath.substring(1);
        }
        
        String baseUrl = getBaseUrl();
        String url = baseUrl + "/" + storagePath;
        
        log.debug("生成访问URL - 模式: {}, 存储路径: {}, 访问URL: {}", 
                accessMode, storagePath, url);
        
        return url;
    }
    
    /**
     * 从完整URL提取存储路径
     * 
     * @param fullUrl 完整URL（如：http://localhost:9000/zhizhi/abc123.jpg）
     * @return 存储路径（如：zhizhi/abc123.jpg）
     */
    public String extractStoragePath(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty()) {
            return null;
        }
        
        // 如果已经是相对路径，直接返回
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            return fullUrl;
        }
        
        // 提取路径部分（从第三个 / 开始）
        int firstSlash = fullUrl.indexOf("://");
        if (firstSlash == -1) {
            return fullUrl;
        }
        
        int thirdSlash = fullUrl.indexOf("/", firstSlash + 3);
        if (thirdSlash == -1) {
            return fullUrl;
        }
        
        String path = fullUrl.substring(thirdSlash + 1);
        log.debug("提取存储路径 - 完整URL: {}, 存储路径: {}", fullUrl, path);
        
        return path;
    }
    
    /**
     * 构建存储路径
     * 
     * @param bucketName 存储桶名称
     * @param fileName 文件名
     * @return 存储路径（如：zhizhi/abc123.jpg）
     */
    public String buildStoragePath(String bucketName, String fileName) {
        if (bucketName == null || bucketName.isEmpty()) {
            return fileName;
        }
        return bucketName + "/" + fileName;
    }
    
    /**
     * 获取基础URL
     */
    private String getBaseUrl() {
        switch (accessMode.toLowerCase()) {
            case "cdn":
                if (cdnBaseUrl != null && !cdnBaseUrl.isEmpty()) {
                    return trimTrailingSlash(cdnBaseUrl);
                }
                log.warn("CDN模式已启用，但未配置CDN URL，fallback到direct模式");
                return trimTrailingSlash(minioBaseUrl);
                
            case "proxy":
                if (proxyBaseUrl != null && !proxyBaseUrl.isEmpty()) {
                    return trimTrailingSlash(proxyBaseUrl);
                }
                log.warn("Proxy模式已启用，但未配置Proxy URL，fallback到direct模式");
                return trimTrailingSlash(minioBaseUrl);
                
            case "direct":
            default:
                return trimTrailingSlash(minioBaseUrl);
        }
    }
    
    /**
     * 移除URL末尾的斜杠
     */
    private String trimTrailingSlash(String url) {
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
    
    /**
     * 验证存储路径格式
     */
    public boolean isValidStoragePath(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) {
            return false;
        }
        
        // 不应该包含协议
        if (storagePath.startsWith("http://") || storagePath.startsWith("https://")) {
            return false;
        }
        
        // 应该包含至少一个路径分隔符（bucket/filename）
        return storagePath.contains("/");
    }
}
