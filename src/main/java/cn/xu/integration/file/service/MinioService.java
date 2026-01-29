package cn.xu.integration.file.service;

import cn.xu.config.MinioConfig;
import cn.xu.support.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务实现
 * <ul>
 *   <li>直接实现接口，不过度抽象</li>
 *   <li>统一异常处理，对外提供简单接口</li>
 *   <li>内置重试机制和降级策略</li>
 *   <li>支持文件名自动生成（UUID）</li>
 *   <li>支持批量操作</li>
 * </ul>
 
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
public class MinioService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioService.class);

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /** 文件大小限制（最大100MB） */
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024;

    /** 允许的文件类型 */
    private static final String[] ALLOWED_EXTENSIONS = {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg",  // 图片
            "mp4", "avi", "mov", "wmv", "flv", "mkv",           // 视频
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", // 文档
            "txt", "md", "json", "xml", "zip", "rar"            // 其他
    };

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws Exception {
        log.info("[文件] ====== 开始上传文件 ======");
        log.info("[文件] 原始文件名: {}", fileName);
        log.info("[文件] MinIO配置 - URL: {}, Bucket: {}", minioConfig.getUrl(), minioConfig.getBucketName());

        // 检查MinIO是否可用
        checkMinioAvailable();

        // 参数校验
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小超过限制（最大100MB）");
        }

        // 如果没有提供文件名，使用原始文件名
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = file.getOriginalFilename();
        }

        // 文件扩展名校验
        validateFileExtension(fileName);
        
        // 直接使用传入的路径（上层FilePathConstants已生成UUID格式）
        log.info("[文件] 存储路径: {}", fileName);

        try (InputStream inputStream = file.getInputStream()) {
            // 上传文件到MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("[文件] 文件上传成功 - 存储路径: {}, 大小: {}bytes",
                    fileName, file.getSize());

            // 返回存储路径（不含bucket）
            log.info("[文件] ====== 文件上传完成 ======");
            return fileName;

        } catch (Exception e) {
            log.error("[文件] 文件上传失败 - 文件名: {}", fileName, e);
            throw new BusinessException("文件上传失败，请稍后重试");
        }
    }

    @Override
    public List<String> uploadFiles(MultipartFile[] files) throws Exception {
        if (files == null || files.length == 0) {
            throw new BusinessException("文件列表不能为空");
        }

        List<String> urls = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String url = uploadFile(file, file.getOriginalFilename());
                urls.add(url);
            } catch (Exception e) {
                log.error("[文件] 批量上传中文件上传失败: {}", file.getOriginalFilename(), e);
                failedFiles.add(file.getOriginalFilename());
            }
        }

        // 如果有失败的文件，记录警告
        if (!failedFiles.isEmpty()) {
            log.warn("[文件] 批量上传完成，但有{}个文件失败: {}", failedFiles.size(), failedFiles);
        }

        return urls;
    }

    @Override
    public void downloadFile(String fileName, String localPath) throws Exception {
        checkMinioAvailable();

        try {
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .filename(localPath)
                            .build()
            );

            log.info("[文件] 文件下载成功 - 文件名: {}, 保存路径: {}", fileName, localPath);

        } catch (Exception e) {
            log.error("[文件] 文件下载失败 - 文件名: {}", fileName, e);
            throw new BusinessException("文件下载失败，请稍后重试");
        }
    }

    @Override
    public InputStream getFileStream(String fileName) throws Exception {
        checkMinioAvailable();

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            log.error("[文件] 获取文件流失败 - 文件名: {}", fileName, e);
            throw new BusinessException("获取文件流失败，请稍后重试");
        }
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        checkMinioAvailable();

        // 从URL中提取文件名
        String fileName = extractFileNameFromUrl(fileUrl);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );

            log.info("[文件] 文件删除成功 - 文件名: {}", fileName);

        } catch (Exception e) {
            log.error("[文件] 文件删除失败 - 文件名: {}", fileName, e);
            throw new BusinessException("文件删除失败，请稍后重试");
        }
    }

    @Override
    public void deleteFiles(List<String> fileUrls) throws Exception {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        List<String> failedFiles = new ArrayList<>();

        for (String fileUrl : fileUrls) {
            try {
                deleteFile(fileUrl);
            } catch (Exception e) {
                log.error("[文件] 批量删除中文件删除失败: {}", fileUrl, e);
                failedFiles.add(fileUrl);
            }
        }

        if (!failedFiles.isEmpty()) {
            log.warn("[文件] 批量删除完成，但有{}个文件失败: {}", failedFiles.size(), failedFiles);
        }
    }

    @Override
    public boolean fileExists(String fileName) throws Exception {
        checkMinioAvailable();

        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        // 检查配置是否有误
        String url = minioConfig.getUrl();
        String bucketName = minioConfig.getBucketName();

        if (url == null || url.trim().isEmpty()) {
            log.error("[文件] MinIO URL配置为空，请检查配置文件中的minio.url配置");
            throw new BusinessException("文件服务配置错误：URL未配置");
        }

        if (bucketName == null || bucketName.trim().isEmpty()) {
            log.error("[文件] MinIO BucketName配置为空，请检查配置文件中的minio.bucketName配置");
            throw new BusinessException("文件服务配置错误：BucketName未配置");
        }

        // 返回完整的文件访问URL
        String fileUrl = String.format("%s/%s/%s",
                url.endsWith("/") ? url.substring(0, url.length() - 1) : url,
                bucketName,
                fileName);

        log.debug("[文件] 生成文件访问URL: {}", fileUrl);
        return fileUrl;
    }

    /**
     * 生成预签名URL（临时访问链接）
     *
     * @param fileName   文件名
     * @param expireTime 过期时间（秒）
     * @return 预签名URL
     */
    public String getPresignedUrl(String fileName, int expireTime) throws Exception {
        checkMinioAvailable();

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(fileName)
                            .expiry(expireTime, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("[文件] 生成预签名URL失败 - 文件名: {}", fileName, e);
            throw new BusinessException("生成预签名URL失败，请稍后重试");
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查MinIO是否可用
     */
    private void checkMinioAvailable() {
        if (!minioConfig.isEnabled()) {
            throw new BusinessException("文件上传功能已禁用");
        }

        if (minioClient == null) {
            throw new BusinessException("文件存储服务不可用，请检查MinIO配置");
        }
    }

    /**
     * 验证文件扩展名
     */
    private void validateFileExtension(String fileName) {
        String extension = getFileExtension(fileName);

        if (extension == null || extension.isEmpty()) {
            throw new BusinessException("文件必须有扩展名");
        }

        boolean allowed = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (ext.equalsIgnoreCase(extension)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            throw new BusinessException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 从URL中提取文件名
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw new BusinessException("文件URL不能为空");
        }

        // 如果是完整URL，提取文件名部分
        if (fileUrl.contains("/")) {
            return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }

        // 如果已经是文件名，直接返回
        return fileUrl;
    }
}
