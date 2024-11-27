package cn.xu.api.controller.admin.file;

import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.file.service.MinioService;
import cn.xu.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("system/file")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public ResponseEntity uploadFile(@RequestPart("file") MultipartFile file, @RequestParam(required = false) String fileName) {
        try {
            if (StringUtils.isEmpty(fileName)) {
                fileName = file.getOriginalFilename();
            }
            String uploadFileUrl = minioService.uploadFile(file, fileName);
            return ResponseEntity.<String>builder()
                    .data(uploadFileUrl)
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("上传成功")
                    .build();
        } catch (Exception e) {
            log.error("上传文件失败: " + e.getMessage());
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "上传文件失败");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity deleteFile(@RequestBody String fileUrl) {
        try {
            // 解析 URL 获取桶名和对象名
            minioService.deleteFile(fileUrl);
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (Exception e) {
            log.error("删除文件失败: " + e.getMessage());
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "删除文件失败");
        }
    }

    @GetMapping("/download/{fileName}")
    public void downloadFile(@PathVariable String fileName, HttpServletResponse response) {
        try {
            String localFilePath = "/tmp/" + fileName;
            minioService.downloadFile(fileName, localFilePath);
        } catch (Exception e) {
            log.error("下载文件失败: " + e.getMessage());
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "下载文件失败");
        }
    }

    @GetMapping("/list")
    public List<String> listFiles() {
        try {
            return minioService.listFiles();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

