package cn.xu.repository;

import cn.xu.model.entity.FileRecord;

import java.util.List;

/**
 * 文件记录仓储接口
 */
public interface FileRecordRepository {

    Long save(FileRecord record);

    FileRecord findById(Long id);

    FileRecord findByStoragePath(String storagePath);

    FileRecord findByFileName(String fileName);

    List<FileRecord> findByUserId(Long userId);

    List<FileRecord> findByUserIdAndStatus(Long userId, Integer status);

    List<FileRecord> findTemporaryFilesBefore(int hours);

    void update(FileRecord record);

    void updateStatus(Long id, Integer status);

    Long countByUserId(Long userId, Integer status);

    void deleteById(Long id);
}
