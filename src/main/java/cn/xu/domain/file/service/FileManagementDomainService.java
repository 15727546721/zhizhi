package cn.xu.domain.file.service;

import cn.xu.domain.file.model.aggregate.FileAggregate;
import cn.xu.domain.file.model.entity.FileEntity;
import cn.xu.domain.file.model.valobj.FileUrl;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件管理领域服务接口
 * 定义文件管理的核心业务能力
 */
public interface FileManagementDomainService {
    
    /**
     * 上传单个文件
     * @param file 上传的文件
     * @param uploadUserId 上传用户ID
     * @return 文件聚合根
     */
    FileAggregate uploadSingleFile(MultipartFile file, Long uploadUserId);
    
    /**
     * 批量上传文件
     * @param files 上传的文件数组
     * @param uploadUserId 上传用户ID
     * @return 文件聚合根
     */
    FileAggregate uploadBatchFiles(MultipartFile[] files, Long uploadUserId);
    
    /**
     * 删除单个文件
     * @param fileUrl 文件URL
     * @param operatorUserId 操作用户ID
     */
    void deleteSingleFile(String fileUrl, Long operatorUserId);
    
    /**
     * 批量删除文件
     * @param fileUrls 文件URL列表
     * @param operatorUserId 操作用户ID
     */
    void deleteBatchFiles(List<String> fileUrls, Long operatorUserId);
    
    /**
     * 将临时文件移动到正式存储
     * @param tempFileUrl 临时文件URL
     * @return 正式文件URL
     */
    String moveToFormalStorage(String tempFileUrl);
    
    /**
     * 批量移动临时文件到正式存储
     * @param tempFileUrls 临时文件URL列表
     * @return 正式文件URL列表
     */
    List<String> moveBatchToFormalStorage(List<String> tempFileUrls);
    
    /**
     * 验证文件访问权限
     * @param fileUrl 文件URL
     * @param userId 用户ID
     * @return 是否有访问权限
     */
    boolean validateFileAccess(String fileUrl, Long userId);
    
    /**
     * 清理过期的临时文件
     * @param expirationHours 过期小时数
     * @return 清理的文件数量
     */
    int cleanupExpiredTemporaryFiles(int expirationHours);
}