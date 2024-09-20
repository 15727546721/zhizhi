package cn.xu.api.controller.file;

import cn.xu.common.Constants;
import cn.xu.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            minioService.uploadFile(file.getOriginalFilename(), file);
            return "File uploaded successfully!";
        } catch (Exception e) {
            return "Error uploading file: " + e.getMessage();
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

