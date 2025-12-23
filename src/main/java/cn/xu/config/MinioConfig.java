package cn.xu.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO文件存储配置
 *
 * 
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private boolean enabled = true; // 默认启用MinIO服务，设置为false可以禁用

    @Bean
    public MinioClient minioClient() {
        // 如果MinIO服务禁用，则不创建MinioClient bean
        if (!enabled) {
            log.info("MinIO服务已禁用，不创建MinioClient bean");
            return null;
        }

        // 配置校验
        if (url == null || url.trim().isEmpty()) {
            log.error("MinIO配置错误：minio.url不能为空");
            log.error("请在application.yml中配置minio.url=http://localhost:9000");
            return null;
        }

        if (bucketName == null || bucketName.trim().isEmpty()) {
            log.error("MinIO配置错误：minio.bucketName不能为空");
            log.error("请在application.yml中配置minio.bucketName=zhizhi");
            return null;
        }

        try {
            log.info("正在初始化MinIO客户端...");
            log.info("MinIO URL: {}", url);
            log.info("MinIO Bucket: {}", bucketName);

            MinioClient client = MinioClient.builder()
                    .endpoint(url)
                    .credentials(accessKey, secretKey)
                    .build();

            // 确保Bucket存在
            initializeBucket(client);

            log.info("MinIO客户端初始化成功，连接地址: {}", url);
            return client;
        } catch (Exception e) {
            log.error("初始化MinIO客户端失败，可能原因：1) MinIO服务未启动  2) URL配置错误  3) 认证信息错误", e);
            return null;
        }
    }

    private void initializeBucket(MinioClient client) {
        try {
            log.info("检查MinIO bucket是否存在: {}", bucketName);
            boolean exists = client.bucketExists(io.minio.BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
                log.info("MinIO bucket不存在，创建新的bucket: {}", bucketName);
                // 创建bucket
                client.makeBucket(io.minio.MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());

                // 设置bucket的访问策略
                String policy = String.format(
                        "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                        bucketName);

                client.setBucketPolicy(io.minio.SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build());

                log.info("成功创建并配置MinIO bucket: {}", bucketName);
            } else {
                log.info("MinIO bucket已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化MinIO bucket失败: {}", bucketName, e);
            // 发生错误时记录异常，方便排查问题
        }
    }
}
