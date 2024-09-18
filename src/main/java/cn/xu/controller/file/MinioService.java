package cn.xu.controller.file;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MinioService {

    @Resource
    private final MinioClient minioClient;
    private final String bucketName;

    @Autowired
    public MinioService(MinioClient minioClient, @Value("${minio.bucketName}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public void uploadFile(String objectName, MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            // 检查桶是否存在，不存在则创建
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 创建 PutObjectArgs 对象
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1) // file.getSize() 为文件大小，-1 表示不设定过期时间
                    .contentType(file.getContentType())
                    .build();

            // 调用 putObject 方法
            minioClient.putObject(putObjectArgs);
        } catch (MinioException e) {
            log.error("Error uploading file to MinIO", e);
            throw new RuntimeException("Error uploading file to MinIO", e);
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
}

