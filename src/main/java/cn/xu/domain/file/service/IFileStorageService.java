package cn.xu.domain.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IFileStorageService {
    String uploadFile(MultipartFile file, String fileName) throws Exception;

    void deleteFile(String fileUrl) throws Exception;

    List<String> uploadFiles(MultipartFile[] files);

    void deleteFiles(List<String> fileUrls);
}
