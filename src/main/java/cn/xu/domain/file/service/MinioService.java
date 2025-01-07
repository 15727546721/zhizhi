package cn.xu.domain.file.service;


import cn.xu.exception.AppException;
import cn.xu.infrastructure.common.ResponseCode;
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

import javax.annotation.Resource;
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

    @Value("${minio.url}")
    private String minioUrl;

    @Resource
    private final MinioClient minioClient;
    private final String bucketName;

    @Autowired
    public MinioService(MinioClient minioClient, @Value("${minio.bucketName}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            // 检查桶是否存在，不存在则创建
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            // 创建 PutObjectArgs 对象
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(uniqueFileName)
                    .stream(inputStream, file.getSize(), -1) // file.getSize() 为文件大小，-1 表示不设定过期时间
                    .contentType(file.getContentType())
                    .build();

            // 调用 putObject 方法
            minioClient.putObject(putObjectArgs);

            // 生成可访问的 URL，有效期为 1 小时
//            return minioClient.getPresignedObjectUrl(
//                    GetPresignedObjectUrlArgs.builder()
//                            .bucket(bucketName)
//                            .object(uniqueFileName)
//                            .expiry(60 * 60) // 1小时有效期
//                            .build()
//            );
//            // 返回永久的可访问URL
            String sharedUrl = minioUrl + "/" + bucketName + "/" + uniqueFileName;

            log.info("文件上传成功，可访问URL为：{}", sharedUrl);
            return sharedUrl;
        } catch (MinioException e) {
            log.error("上传文件到MinIO失败", e);
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "上传文件失败");
        }
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        log.info("开始删除文件: {}", fileUrl);
        JSONObject jsonObject = JSONObject.parseObject(fileUrl);
        fileUrl = jsonObject.getString("fileUrl");
        if (StringUtils.isEmpty(fileUrl)) {
            log.error("文件URL为空，无法删除");
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "文件URL为空，无法删除");
        }
        try {
            String objectName = fileUrl.substring(fileUrl.indexOf(bucketName) + bucketName.length() + 1);

            // 构造 RemoveObjectArgs
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            // 调用 removeObject 方法删除文件
            minioClient.removeObject(removeObjectArgs);
            log.info("文件删除成功: {}", objectName);
        } catch (MinioException e) {
            log.error("删除文件失败: {}", e.getMessage());
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "删除文件失败");
        }
    }


    public void downloadFile(String objectName, String localFilePath) throws Exception {
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
        // 检查文件大小（例如限制为5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "图片大小不能超过5MB");
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
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "上传图片失败：" + e.getMessage());
        }
    }

    /**
     * 检查并创建存储桶
     */
    private void createBucketIfNotExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
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
            }
        } catch (Exception e) {
            log.error("创建MinIO存储桶失败", e);
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "创建存储空间失败");
        }
    }

    /**
     * 批量删除话题图片
     */
    public void deleteTopicImages(List<String> imageUrls) {
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
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "处理图片失败：" + e.getMessage());
        }
    }

    /**
     * 批量移动临时图片到永久存储目录
     *
     * @param tempImageUrls 临时图片URL列表
     * @return 永久图片URL列表
     */
    public List<String> moveToFormal(List<String> tempImageUrls) {
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
}