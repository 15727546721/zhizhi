package cn.xu.integration.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 文件存储服务接口
 * 抽象文件存储操作，支持多种存储实现（MinIO、OSS、本地等）
 * 
 * DDD实用主义：
 * - 接口简洁，只包含核心操作
 * - 避免过度抽象，直接使用Spring的MultipartFile
 * - 返回值简单明了，方便使用
 */
public interface FileStorageService {
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param fileName 文件名（包含扩展名）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String fileName) throws Exception;
    
    /**
     * 批量上传文件
     * 
     * @param files 文件数组
     * @return 文件访问URL列表
     */
    List<String> uploadFiles(MultipartFile[] files) throws Exception;
    
    /**
     * 下载文件
     * 
     * @param fileName 文件名
     * @param localPath 本地保存路径
     */
    void downloadFile(String fileName, String localPath) throws Exception;
    
    /**
     * 获取文件流
     * 
     * @param fileName 文件名
     * @return 文件输入流
     */
    InputStream getFileStream(String fileName) throws Exception;
    
    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL或文件名
     */
    void deleteFile(String fileUrl) throws Exception;
    
    /**
     * 批量删除文件
     * 
     * @param fileUrls 文件URL或文件名列表
     */
    void deleteFiles(List<String> fileUrls) throws Exception;
    
    /**
     * 检查文件是否存在
     * 
     * @param fileName 文件名
     * @return 是否存在
     */
    boolean fileExists(String fileName) throws Exception;
    
    /**
     * 获取文件访问URL
     * 
     * @param fileName 文件名
     * @return 完整的访问URL
     */
    String getFileUrl(String fileName);
}
