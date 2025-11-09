package cn.xu.domain.file.service;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import com.alibaba.fastjson2.JSONObject;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class MinioService implements IFileStorageService {

    private MinioClient minioClient;
    private final String bucketName;
    private final String minioUrl;
    private final boolean minioEnabled;

    public MinioService(@Value("${minio.bucketName:zhizhi}") String bucketName,
                       @Value("${minio.url:http://localhost:9000}") String minioUrl,
                       @Value("${minio.enabled:true}") boolean minioEnabled) {
        this.bucketName = bucketName;
        this.minioUrl = minioUrl;
        this.minioEnabled = minioEnabled;
        
        if (!minioEnabled) {
            log.warn("MinIO功能未启用，文件上传功能将不可用");
        }
    }

    @Autowired(required = false)
    public void setMinioClient(MinioClient minioClient) {
        this.minioClient = minioClient;
        if (minioClient == null) {
            log.warn("MinIO客户端未初始化，文件上传功能将不可用");
        } else {
            log.info("MinIO客户端初始化成功");
        }
    }

    // Base62 字符集
    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Base62 编码方法
    private String base62Encode(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(BASE62_ALPHABET.charAt((int)(num % 62)));
            num /= 62;
        }
        return sb.reverse().toString();
    }

    // 生成 16 位长度的唯一文件名
    private String generateUniqueFileName(String originalFileName) {
        // 组合时间戳和UUID，确保高唯一性
        long timestamp = System.nanoTime(); // 使用纳秒级时间戳
        UUID uuid = UUID.randomUUID();
        long combined = timestamp ^ uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();

        String base62FileName = base62Encode(combined);

        // 获取文件扩展名
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

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
            // 取后16位（最新生成的部分）
            base62FileName = base62FileName.substring(base62FileName.length() - targetLength);
        }

        return base62FileName + extension;
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws Exception {
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件上传功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件上传服务不可用");
        }
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件不能为空");
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            checkAndCreateBucket();
            
            String finalFileName;
            if (fileName != null && !fileName.trim().isEmpty()) {
                finalFileName = fileName.replaceAll("[^a-zA-Z0-9._/-]", "_");
                if (finalFileName.contains("..")) {
                    throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件名包含非法字符");
                }
                if (finalFileName.startsWith("/")) {
                    finalFileName = finalFileName.substring(1);
                }
            } else {
                finalFileName = generateUniqueFileName(Objects.requireNonNull(file.getOriginalFilename()));
            }

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(finalFileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                    .build();

            minioClient.putObject(putObjectArgs);

            String sharedUrl = minioUrl + "/" + bucketName + "/" + finalFileName;
            log.info("文件上传成功，可访问URL为：{}", sharedUrl);
            return sharedUrl;
        } catch (MinioException e) {
            log.error("上传文件到MinIO失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("上传文件失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传文件失败：" + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件删除功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件删除服务不可用");
        }
        
        if (StringUtils.isEmpty(fileUrl)) {
            return;
        }
        
        log.info("开始删除文件: {}", fileUrl);
        
        try {
            String actualFileUrl = fileUrl;
            if (fileUrl.trim().startsWith("{")) {
                try {
                    JSONObject jsonObject = JSONObject.parseObject(fileUrl);
                    actualFileUrl = jsonObject.getString("fileUrl");
                    if (StringUtils.isEmpty(actualFileUrl)) {
                        return;
                    }
                } catch (Exception e) {
                    log.debug("fileUrl不是JSON格式，使用原始URL: {}", fileUrl);
                }
            }
            
            String objectName;
            int bucketIndex = actualFileUrl.indexOf(bucketName);
            if (bucketIndex >= 0) {
                objectName = actualFileUrl.substring(bucketIndex + bucketName.length() + 1);
            } else {
                if (actualFileUrl.contains("/")) {
                    objectName = actualFileUrl.substring(actualFileUrl.lastIndexOf("/") + 1);
                } else {
                    objectName = actualFileUrl;
                }
                log.warn("URL中未找到bucketName，使用提取的对象名: {}", objectName);
            }
            
            if (StringUtils.isEmpty(objectName)) {
                log.warn("无法从URL中提取对象名，跳过删除: {}", actualFileUrl);
                return;
            }

            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.removeObject(removeObjectArgs);
            log.info("文件删除成功: {}", objectName);
        } catch (MinioException e) {
            log.error("删除文件失败: {}", e.getMessage(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除文件失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("删除文件时发生异常: {}", fileUrl, e);
            throw e;
        }
    }

    // 批量上传文件
    @Override
    public List<String> uploadFiles(MultipartFile[] files) {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件上传功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件上传服务不可用");
        }
        
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileUrl = null;
            try {
                // 使用生成的唯一文件名来上传
                String uniqueFileName = generateUniqueFileName(Objects.requireNonNull(file.getOriginalFilename()));
                fileUrl = uploadFile(file, uniqueFileName);
            } catch (Exception e) {
                log.error("上传文件失败: {}", file.getOriginalFilename(), e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传文件失败：" + e.getMessage());
            }
            fileUrls.add(fileUrl);
        }
        return fileUrls;
    }

    // 批量删除文件
    @Override
    public void deleteFiles(List<String> fileUrls) {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件删除功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件删除服务不可用");
        }
        
        for (String fileUrl : fileUrls) {
            try {
                deleteFile(fileUrl);
            } catch (Exception e) {
                log.error("删除文件失败: {}", fileUrl, e);
            }
        }
    }

    public void downloadFile(String objectName, String localFilePath) throws Exception {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件下载功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件下载服务不可用");
        }
        
        try {
            // 构造 GetObjectArgs
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            // 调用 getObject 方法获取 InputStream
            GetObjectResponse response = minioClient.getObject(getObjectArgs);

            // 将 InputStream 写入到本地文件
            try (InputStream inputStream = response;
                 OutputStream outputStream = new FileOutputStream(new File(localFilePath))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (MinioException e) {
            throw new RuntimeException("Error downloading file from MinIO", e);
        }
    }

    public List<String> listFiles() throws Exception {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件列表功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件列表服务不可用");
        }
        
        try {
            List<String> fileNames = new ArrayList<>();
            // 使用 ListObjectsArgs 构造参数
            ListObjectsArgs args = ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .build();
            Iterable<Result<Item>> results = minioClient.listObjects(args);
            for (Result<Item> result : results) {
                fileNames.add(result.get().objectName());
            }
            return fileNames;
        } catch (MinioException e) {
            throw new RuntimeException("Error listing files from MinIO", e);
        }
    }

    /**
     * 上传话题图片
     *
     * @param file     图片文件
     * @param filePath 文件存储路径
     * @return 图片访问URL
     */
    public String uploadTopicImage(MultipartFile file, String filePath) {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件上传功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件上传服务不可用");
        }
        
        // 检查文件大小（例如限制为5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "图片大小不能超过5MB");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // 检查桶是否存在
            createBucketIfNotExists();

            // 构建文件元数据
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", file.getContentType());

            // 创建PutObjectArgs对象
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .headers(headers)
                    .build();

            // 上传文件
            minioClient.putObject(putObjectArgs);

            // 返回可访问的URL
            String imageUrl = minioUrl + "/" + bucketName + "/" + filePath;
            log.info("话题图片上传成功，访问URL: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            log.error("上传话题图片到MinIO失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传图片失败：" + e.getMessage());
        }
    }

    /**
     * 检查并创建存储桶
     */
    private void createBucketIfNotExists() {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，无法创建存储桶");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "存储服务不可用");
        }
        
        try {
            log.info("开始检查MinIO bucket是否存在: {}", bucketName);
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                log.info("MinIO bucket不存在，开始创建: {}", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );

                // 设置桶策略为公共读
                String policy = String.format(
                        "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                        bucketName
                );
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build()
                );
                log.info("成功创建并配置MinIO bucket: {}", bucketName);
            } else {
                log.info("MinIO bucket已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("创建MinIO存储桶失败: {}", bucketName, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "创建存储空间失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除话题图片
     */
    public void deleteTopicImages(List<String> imageUrls) {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件删除功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件删除服务不可用");
        }
        
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (String imageUrl : imageUrls) {
            try {
                String objectName = imageUrl.substring(imageUrl.indexOf(bucketName) + bucketName.length() + 1);
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .build()
                );
                log.info("成功删除话题图片: {}", objectName);
            } catch (Exception e) {
                log.error("删除话题图片失败: {}", imageUrl, e);
            }
        }
    }

    /**
     * 将临时图片移动到永久存储目录
     *
     * @param tempImageUrl 临时图片URL
     * @return 永久图片URL
     */
    public String moveToFormal(String tempImageUrl) {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件移动功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件移动服务不可用");
        }
        
        if (!tempImageUrl.contains("/temp/")) {
            return tempImageUrl; // 如果不是临时图片，直接返回原URL
        }

        try {
            // 1. 从URL中提取文件名
            String tempObjectName = tempImageUrl.substring(tempImageUrl.indexOf(bucketName) + bucketName.length() + 1);
            String fileName = tempObjectName.substring(tempObjectName.lastIndexOf("/") + 1);

            // 2. 生成永久存储路径 (formal/年月/文件名)
            String formalPath = String.format("formal/%s/%s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")),
                    fileName);

            // 3. 复制文件到新位置
            CopyObjectArgs copyArgs = CopyObjectArgs.builder()
                    .bucket(bucketName)
                    .object(formalPath)
                    .source(CopySource.builder()
                            .bucket(bucketName)
                            .object(tempObjectName)
                            .build())
                    .build();
            minioClient.copyObject(copyArgs);

            // 4. 删除临时文件
            RemoveObjectArgs removeArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(tempObjectName)
                    .build();
            minioClient.removeObject(removeArgs);

            // 5. 返回新的URL
            String formalUrl = minioUrl + "/" + bucketName + "/" + formalPath;
            log.info("图片已从临时目录移动到永久目录: {} -> {}", tempImageUrl, formalUrl);
            return formalUrl;

        } catch (Exception e) {
            log.error("移动图片到永久目录失败: {}", tempImageUrl, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "处理图片失败：" + e.getMessage());
        }
    }

    /**
     * 批量移动临时图片到永久存储目录
     *
     * @param tempImageUrls 临时图片URL列表
     * @return 永久图片URL列表
     */
    public List<String> moveToFormal(List<String> tempImageUrls) {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，文件移动功能不可用");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件移动服务不可用");
        }
        
        if (tempImageUrls == null || tempImageUrls.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> formalUrls = new ArrayList<>();
        for (String tempUrl : tempImageUrls) {
            try {
                String formalUrl = moveToFormal(tempUrl);
                formalUrls.add(formalUrl);
            } catch (Exception e) {
                log.error("移动图片失败: {}", tempUrl, e);
                // 如果有失败，需要回滚已经移动的文件
                if (!formalUrls.isEmpty()) {
                    deleteTopicImages(formalUrls);
                }
                throw e;
            }
        }
        return formalUrls;
    }
    
    /**
     * 检查MinIO服务是否可用
     * @return true表示可用，false表示不可用
     */
    public boolean isMinioAvailable() {
        return minioEnabled && minioClient != null;
    }

    /**
     * 检查并创建存储桶（增强版）
     */
    private void checkAndCreateBucket() {
        // 检查MinIO是否启用
        if (!minioEnabled || minioClient == null) {
            log.warn("MinIO功能未启用或客户端未初始化，无法创建存储桶");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "存储服务不可用");
        }
        
        try {
            log.info("开始检查MinIO bucket是否存在: {}", bucketName);
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                log.info("MinIO bucket不存在，开始创建: {}", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );

                // 设置桶策略为公共读
                String policy = String.format(
                        "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                        bucketName
                );
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build()
                );
                log.info("成功创建并配置MinIO bucket: {}", bucketName);
            } else {
                log.info("MinIO bucket已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查或创建MinIO存储桶失败: {}", bucketName, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "存储空间检查或创建失败: " + e.getMessage());
        }
    }
}