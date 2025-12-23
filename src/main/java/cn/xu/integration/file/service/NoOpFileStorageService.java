package cn.xu.integration.file.service;

import cn.xu.support.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 空的文件存储服务实现
 * <p>当MinIO禁用时使用，所有操作都会抛出异常提示功能未启用</p>
 
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "minio.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpFileStorageService implements FileStorageService {
    
    private static final String DISABLED_MESSAGE = "文件存储服务未启用，请配置minio.enabled=true";
    
    public NoOpFileStorageService() {
        log.warn("[文件] ⚠️ 文件存储服务未启用（MinIO disabled），文件上传/下载功能不可用");
    }
    
    @Override
    public String uploadFile(MultipartFile file, String fileName) throws Exception {
        throw new BusinessException(DISABLED_MESSAGE);
    }
    
    @Override
    public List<String> uploadFiles(MultipartFile[] files) throws Exception {
        throw new BusinessException(DISABLED_MESSAGE);
    }
    
    @Override
    public void downloadFile(String fileName, String localPath) throws Exception {
        throw new BusinessException(DISABLED_MESSAGE);
    }
    
    @Override
    public InputStream getFileStream(String fileName) throws Exception {
        throw new BusinessException(DISABLED_MESSAGE);
    }
    
    @Override
    public void deleteFile(String fileUrl) throws Exception {
        log.warn("[文件] 文件存储服务未启用，跳过删除文件: {}", fileUrl);
    }
    
    @Override
    public void deleteFiles(List<String> fileUrls) throws Exception {
        log.warn("[文件] 文件存储服务未启用，跳过删除文件: {}", fileUrls);
    }
    
    @Override
    public boolean fileExists(String fileName) throws Exception {
        return false;
    }
    
    @Override
    public String getFileUrl(String fileName) {
        return "";
    }
}
