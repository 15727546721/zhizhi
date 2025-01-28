package cn.xu.infrastructure.config;

import io.minio.MinioClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
@Slf4j
public class MinioConfig {

    @Value("${minio.url}")
    private String url;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    // 新增配置项
    private long maxFileSize = 10 * 1024 * 1024; // 默认最大10MB
    private List<String> allowedIps; // 允许访问的IP列表

    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(url)
                    .credentials(accessKey, secretKey)
                    .build();
            
            // 检查并初始化bucket
            initializeBucket(client);
            
            return client;
        } catch (Exception e) {
            log.error("初始化MinIO客户端失败", e);
            throw new RuntimeException("Failed to initialize MinIO client", e);
        }
    }

    private void initializeBucket(MinioClient client) {
        try {
            boolean exists = client.bucketExists(io.minio.BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!exists) {
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
            }
        } catch (Exception e) {
            log.error("初始化MinIO bucket失败", e);
            throw new RuntimeException("Failed to initialize MinIO bucket", e);
        }
    }
}

