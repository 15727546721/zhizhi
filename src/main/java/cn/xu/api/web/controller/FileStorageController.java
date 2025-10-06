package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.application.service.FileApplicationService;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/file")
@Slf4j
@Tag(name = "文件存储接口", description = "文件上传和删除相关接口")
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
            // 从上下文获取用户ID
            Long uploadUserId = StpUtil.getLoginIdAsLong();
            
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
            // 从上下文获取用户ID
            Long operatorUserId = StpUtil.getLoginIdAsLong();
            
            fileApplicationService.deleteFiles(fileUrls, operatorUserId);
            return ResponseEntity.success();
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量删除文件失败");
        }
    }
}