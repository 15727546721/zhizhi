package cn.xu.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
@Slf4j
public class MinioConfig {

    private String url;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private boolean enabled = true; // 添加一个开关，允许禁用MinIO

    @Bean
    public MinioClient minioClient() {
        // 如果MinIO被禁用，不创建bean
        if (!enabled) {
            log.info("MinIO功能已禁用，不会创建MinioClient bean");
            return null;
        }
        
        // 配置验证
        if (url == null || url.trim().isEmpty()) {
            log.error("MinIO配置错误：minio.url未配置或为空");
            log.error("请在application.yml中配置：minio.url=http://localhost:9000");
            return null;
        }
        
        if (bucketName == null || bucketName.trim().isEmpty()) {
            log.error("MinIO配置错误：minio.bucketName未配置或为空");
            log.error("请在application.yml中配置：minio.bucketName=zhizhi");
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
            
            // 检查并初始化bucket
            initializeBucket(client);
            
            log.info("✅ 成功初始化MinIO客户端，连接到: {}", url);
            return client;
        } catch (Exception e) {
            log.error("❌ 初始化MinIO客户端失败，将禁用文件上传功能", e);
            log.error("请检查：1) MinIO服务是否启动  2) URL配置是否正确  3) 认证信息是否正确");
            return null;
        }
    }

    private void initializeBucket(MinioClient client) {
        try {
            log.info("开始检查MinIO bucket是否存在: {}", bucketName);
            boolean exists = client.bucketExists(io.minio.BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
                log.info("MinIO bucket不存在，开始创建: {}", bucketName);
                // 创建bucket
                client.makeBucket(io.minio.MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());

                // 设置bucket为公共读
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
            // 不抛出异常，而是简单地记录错误
            // 这样即使bucket初始化失败，应用仍然可以启动
        }
    }
    
    // 添加getter方法
    public boolean isEnabled() {
        return enabled;
    }
}