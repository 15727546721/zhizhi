package cn.xu.domain.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件存储服务接口
 * 作为防腐层，隔离外部存储服务的具体实现
 */
public interface IFileStorageService {
    
    /**
     * 上传单个文件
     * @param file 文件
     * @param fileName 文件名
     * @return 文件访问URL
     * @throws Exception 上传异常
     */
    String uploadFile(MultipartFile file, String fileName) throws Exception;

    /**
     * 删除单个文件
     * @param fileUrl 文件URL
     * @throws Exception 删除异常
     */
    void deleteFile(String fileUrl) throws Exception;

    /**
     * 批量上传文件
     * @param files 文件数组
     * @return 文件URL列表
     */
    List<String> uploadFiles(MultipartFile[] files);

    /**
     * 批量删除文件
     * @param fileUrls 文件URL列表
     */
    void deleteFiles(List<String> fileUrls);
    
    /**
     * 移动临时文件到正式存储
     * @param tempFileUrl 临时文件URL
     * @return 正式文件URL
     */
    String moveToFormal(String tempFileUrl);
    
    /**
     * 批量移动临时文件到正式存储
     * @param tempFileUrls 临时文件URL列表
     * @return 正式文件URL列表
     */
    List<String> moveToFormal(List<String> tempFileUrls);
}
