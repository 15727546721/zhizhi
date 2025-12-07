package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.service.file.FileManagementService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件存储控制器
 *
 * <p>提供文件上传、批量删除等功能接口，使用MinIO作为底层存储</p>

 */
@RestController
@RequestMapping("/api/file")
@Slf4j
@Tag(name = "文件存储接口", description = "文件上传和删除相关接口")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileManagementService fileManagementService;

    /**
     * 通用文件上传
     *
     * <p>支持批量上传文件，使用MinIO存储
     * <p>需要登录后才能访问
     *
     * @param files 文件数组
     * @return 上传成功的文件URL列表
     * @throws BusinessException 当上传失败时抛出
     */
    @PostMapping("/upload")
    @ApiOperationLog(description = "文件上传")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        log.info("========== 文件上传接口被调用 ==========");
        log.info("接收了 {} 个文件", files != null ? files.length : 0);

        try {
            // 从上下文获取用户ID
            Long uploadUserId = StpUtil.getLoginIdAsLong();
            log.info("当前用户ID: {}", uploadUserId);

            // 使用MinIO服务批量上传（不关联业务）
            log.info("调用 fileManagementService.uploadFiles...");
            List<String> fileUrls = fileManagementService.uploadFiles(files, uploadUserId);

            log.info("上传完成，返回了 {} 个URL", fileUrls != null ? fileUrls.size() : 0);
            if (fileUrls != null && !fileUrls.isEmpty()) {
                for (int i = 0; i < fileUrls.size(); i++) {
                    log.info("URL[{}]: {}", i, fileUrls.get(i));
                }
            }

            log.info("========== 文件上传接口完成 ==========");
            return ResponseEntity.success(fileUrls);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     *
     * <p>根据文件URL列表批量删除文件
     * <p>需要登录后才能访问
     *
     * @param fileUrls 文件URL列表
     * @return 删除结果
     * @throws BusinessException 当删除失败时抛出
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

            // 批量删除文件
            for (String fileUrl : fileUrls) {
                fileManagementService.deleteFile(fileUrl, operatorUserId);
            }
            return ResponseEntity.success();
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量删除文件失败: " + e.getMessage());
        }
    }
}