package cn.xu.application.service;

import cn.xu.domain.file.model.aggregate.FileAggregate;
import cn.xu.domain.file.service.FileManagementDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文件应用服务
 * 协调文件管理的业务流程，处理事务边界
 */
@Service
@Slf4j
public class FileApplicationService {
    
    @Resource
    private FileManagementDomainService fileManagementDomainService;
    
    /**
     * 上传文件
     */
    @Transactional(rollbackFor = Exception.class)
    public List<String> uploadFiles(MultipartFile[] files, Long uploadUserId) {
        log.info("开始处理文件上传请求，文件数量: {}, 用户ID: {}", files.length, uploadUserId);
        
        FileAggregate fileAggregate;
        if (files.length == 1) {
            fileAggregate = fileManagementDomainService.uploadSingleFile(files[0], uploadUserId);
        } else {
            fileAggregate = fileManagementDomainService.uploadBatchFiles(files, uploadUserId);
        }
        
        return fileAggregate.getAllFileUrls();
    }
    
    /**
     * 删除文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFiles(List<String> fileUrls, Long operatorUserId) {
        if (fileUrls.size() == 1) {
            fileManagementDomainService.deleteSingleFile(fileUrls.get(0), operatorUserId);
        } else {
            fileManagementDomainService.deleteBatchFiles(fileUrls, operatorUserId);
        }
    }
}