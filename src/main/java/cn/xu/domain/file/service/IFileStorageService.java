package cn.xu.domain.file.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {
    String uploadFile(MultipartFile file, String fileName) throws Exception;

    void deleteFile(String fileUrl) throws Exception;
}
