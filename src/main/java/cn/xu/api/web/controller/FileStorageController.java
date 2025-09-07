package cn.xu.api.web.controller;

import cn.xu.application.common.ResponseCode;
import cn.xu.application.service.FileApplicationService;
import cn.xu.infrastructure.common.annotation.ApiOperationLog;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.response.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@Slf4j
public class FileStorageController {

    @Resource
    private FileApplicationService fileApplicationService;

    /**
     * 通用文件上传
     */
    @PostMapping("/upload")
    @ApiOperationLog(description = "文件上传")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        try {
            // TODO: 从上下文获取用户ID，这里暂时使用默认值
            Long uploadUserId = 1L;
            
            List<String> fileUrls = fileApplicationService.uploadFiles(files, uploadUserId);
            return ResponseEntity.success(fileUrls);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件上传失败");
        }
    }

    /**
     * 批量删除文件
     */
    @PostMapping("/deleteBatch")
    @ApiOperationLog(description = "批量删除文件")
    public ResponseEntity<Void> deleteFiles(@RequestBody List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "fileUrls不能为空");
        }
        try {
            // TODO: 从上下文获取用户ID，这里暂时使用默认值
            Long operatorUserId = 1L;
            
            fileApplicationService.deleteFiles(fileUrls, operatorUserId);
            return ResponseEntity.success();
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量删除文件失败");
        }
    }
}
