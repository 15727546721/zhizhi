package cn.xu.api.system.controller;


import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.file.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "文件管理", description = "文件管理相关接口")
@Slf4j
@RestController
@RequestMapping("system/file")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到文件存储系统")
    @ApiOperationLog(description = "上传文件")
    public ResponseEntity uploadFile(@Parameter(description = "文件") @RequestPart("files") MultipartFile file, @Parameter(description = "文件名(可选)") @RequestParam(required = false) String fileName) {
        try {
            if (StringUtils.isEmpty(fileName)) {
                fileName = file.getOriginalFilename();
            }
            String uploadFileUrl = minioService.uploadFile(file, fileName);
            return ResponseEntity.<String>builder()
                    .data(uploadFileUrl)
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("上传成功")
                    .build();
        } catch (Exception e) {
            log.error("上传文件失败: " + e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传文件失败");
        }
    }

    @PostMapping("/delete")
    @Operation(summary = "删除文件", description = "从文件存储系统删除指定文件")
    @ApiOperationLog(description = "删除文件")
    public ResponseEntity deleteFile(@Parameter(description = "文件URL") @RequestBody String fileUrl) {
        try {
            // 解析 URL 获取桶名和对象名
            minioService.deleteFile(fileUrl);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (Exception e) {
            log.error("删除文件失败: " + e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除文件失败");
        }
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "下载文件", description = "从文件存储系统下载指定文件")
    @ApiOperationLog(description = "下载文件")
    public void downloadFile(@Parameter(description = "文件名") @PathVariable String fileName, HttpServletResponse response) {
        try {
            String localFilePath = "/tmp/" + fileName;
            minioService.downloadFile(fileName, localFilePath);
        } catch (Exception e) {
            log.error("下载文件失败: " + e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "下载文件失败");
        }
    }

    @GetMapping("/list")
    @Operation(summary = "列出文件", description = "列出文件存储系统中的所有文件")
    @ApiOperationLog(description = "列出文件")
    public List<String> listFiles() {
        try {
            return minioService.listFiles();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}