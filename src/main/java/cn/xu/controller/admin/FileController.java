package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.config.MinioConfig;
import cn.xu.integration.file.service.MinioService;
import cn.xu.support.exception.BusinessException;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件管理控制器
 *
 * <p>提供后台文件上传、下载、删除功能
 * <p>仅在minio.enabled=true时启用
 *
 *
 */
@Tag(name = "文件管理", description = "文件管理相关接口")
@Slf4j
@RestController
@RequestMapping("/api/system/file")
@ConditionalOnProperty(name = "minio.enabled", havingValue = "true", matchIfMissing = false)
public class FileController {

    private final MinioService minioService;
    
    @Autowired
    private MinioClient minioClient;
    
    @Autowired
    private MinioConfig minioConfig;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    /**
     * 获取文件列表
     *
     * <p>分页查询MinIO中的文件列表
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param prefix 文件前缀（可选，用于筛选）
     * @return 分页的文件列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取文件列表", description = "分页查询文件存储系统中的文件")
    @SaCheckLogin
    @SaCheckPermission("system:file:list")
    @ApiOperationLog(description = "获取文件列表")
    public ResponseEntity<PageResponse<List<FileVO>>> getFileList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "文件前缀") @RequestParam(required = false) String prefix) {
        log.info("获取文件列表: pageNo={}, pageSize={}, prefix={}", pageNo, pageSize, prefix);
        
        List<FileVO> fileList = new ArrayList<>();
        long total = 0;
        
        try {
            // 构建查询参数
            ListObjectsArgs.Builder argsBuilder = ListObjectsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .recursive(true);
            
            if (StringUtils.isNotEmpty(prefix)) {
                argsBuilder.prefix(prefix);
            }
            
            // 查询所有文件
            List<FileVO> allFiles = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(argsBuilder.build());
            
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();
                
                FileVO fileVO = FileVO.builder()
                        .fileName(objectName)
                        .fileUrl(minioService.getFileUrl(objectName))
                        .fileSize(item.size())
                        .fileSizeStr(formatFileSize(item.size()))
                        .fileType(getFileType(objectName))
                        .lastModified(item.lastModified() != null 
                                ? LocalDateTime.ofInstant(item.lastModified().toInstant(), ZoneId.systemDefault())
                                : null)
                        .build();
                allFiles.add(fileVO);
            }
            
            // 按修改时间倒序排序
            allFiles.sort((a, b) -> {
                if (a.getLastModified() == null) return 1;
                if (b.getLastModified() == null) return -1;
                return b.getLastModified().compareTo(a.getLastModified());
            });
            
            total = allFiles.size();
            
            // 分页处理
            int start = (pageNo - 1) * pageSize;
            int end = Math.min(start + pageSize, allFiles.size());
            if (start < allFiles.size()) {
                fileList = allFiles.subList(start, end);
            }
            
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage());
        }
        
        PageResponse<List<FileVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, total, fileList);
        
        return ResponseEntity.<PageResponse<List<FileVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    /**
     * 批量删除文件
     *
     * @param fileUrls 文件URL列表
     * @return 删除结果
     */
    @DeleteMapping("/batchDelete")
    @Operation(summary = "批量删除文件", description = "批量删除文件存储系统中的文件")
    @SaCheckLogin
    @SaCheckPermission("system:file:delete")
    @ApiOperationLog(description = "批量删除文件")
    public ResponseEntity<Void> batchDeleteFiles(@RequestBody List<String> fileUrls) {
        log.info("批量删除文件: count={}", fileUrls.size());
        try {
            minioService.deleteFiles(fileUrls);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (Exception e) {
            log.error("批量删除文件失败: {}", e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除文件失败");
        }
    }

    /**
     * 上传文件
     *
     * <p>上传文件到MinIO存储系统
     *
     * @param file 文件
     * @param fileName 自定义文件名（可选）
     * @return 上传成功的文件URL
     * @throws BusinessException 当上传失败时抛出
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到文件存储系统")
    @ApiOperationLog(description = "上传文件")
    public ResponseEntity uploadFile(@Parameter(description = "文件") @RequestPart("files") MultipartFile file,
                                     @Parameter(description = "文件名（可选）") @RequestParam(required = false) String fileName) {
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
            log.error("上传文件失败: {}", e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传文件失败");
        }
    }

    /**
     * 删除文件
     *
     * <p>从MinIO存储系统删除指定文件
     *
     * @param fileUrl 文件URL
     * @return 删除结果
     * @throws BusinessException 当删除失败时抛出
     */
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
            log.error("删除文件失败: {}", e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除文件失败");
        }
    }

    /**
     * 下载文件
     *
     * <p>从MinIO存储系统下载指定文件
     *
     * @param fileName 文件名
     * @param response HTTP响应对象
     * @throws BusinessException 当下载失败时抛出
     */
    @GetMapping("/download/{fileName}")
    @Operation(summary = "下载文件", description = "从文件存储系统下载指定文件")
    @ApiOperationLog(description = "下载文件")
    public void downloadFile(@Parameter(description = "文件名") @PathVariable String fileName, HttpServletResponse response) {
        try {
            String safeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
            String localFilePath = "/tmp/" + safeFileName;
            minioService.downloadFile(fileName, localFilePath);
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage());
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "下载文件失败");
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "other";
        }
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if ("jpg,jpeg,png,gif,bmp,webp,svg".contains(ext)) {
            return "image";
        } else if ("mp4,avi,mov,wmv,flv,mkv".contains(ext)) {
            return "video";
        } else if ("pdf,doc,docx,xls,xlsx,ppt,pptx,txt,md".contains(ext)) {
            return "document";
        } else if ("zip,rar,7z,tar,gz".contains(ext)) {
            return "archive";
        }
        return "other";
    }

    // ==================== VO类定义 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileVO {
        /** 文件名 */
        private String fileName;
        /** 文件URL */
        private String fileUrl;
        /** 文件大小（字节） */
        private Long fileSize;
        /** 文件大小（格式化） */
        private String fileSizeStr;
        /** 文件类型：image/video/document/archive/other */
        private String fileType;
        /** 最后修改时间 */
        private LocalDateTime lastModified;
    }
}