package cn.xu.repository.impl;

import cn.xu.model.entity.FileRecord;
import cn.xu.repository.mapper.FileRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件记录仓储实现
 * <p>负责文件记录的持久化操作</p>

 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FileRecordRepository {
    
    private final FileRecordMapper fileRecordMapper;
    
    /**
     * 保存文件记录
     */
    public Long save(FileRecord record) {
        fileRecordMapper.insert(record);
        return record.getId();
    }
    
    /**
     * 根据ID查询
     */
    public FileRecord findById(Long id) {
        return fileRecordMapper.findById(id);
    }
    
    /**
     * 根据存储路径查询
     */
    public FileRecord findByStoragePath(String storagePath) {
        return fileRecordMapper.findByStoragePath(storagePath);
    }
    
    /**
     * 根据文件名查询
     */
    public FileRecord findByFileName(String fileName) {
        return fileRecordMapper.findByFileName(fileName);
    }
    
    /**
     * 查询用户上传的文件（所有状态）
     */
    public List<FileRecord> findByUserId(Long userId) {
        return fileRecordMapper.findByUserId(userId, null);
    }
    
    /**
     * 查询用户上传的文件（按状态筛选）
     */
    public List<FileRecord> findByUserIdAndStatus(Long userId, Integer status) {
        return fileRecordMapper.findByUserId(userId, status);
    }
    
    /**
     * 查询指定时间之前的临时文件
     */
    public List<FileRecord> findTemporaryFilesBefore(int hours) {
        LocalDateTime beforeTime = LocalDateTime.now().minusHours(hours);
        return fileRecordMapper.findTemporaryFilesBefore(beforeTime);
    }
    
    /**
     * 更新文件记录
     */
    public void update(FileRecord record) {
        record.setUpdateTime(LocalDateTime.now());
        fileRecordMapper.update(record);
    }
    
    /**
     * 更新文件状态
     */
    public void updateStatus(Long id, Integer status) {
        fileRecordMapper.updateStatus(id, status);
    }
    
    /**
     * 统计用户文件数量（按状态）
     */
    public Long countByUserId(Long userId, Integer status) {
        return fileRecordMapper.countByUserId(userId, status);
    }
    
    /**
     * 删除文件记录
     */
    public void deleteById(Long id) {
        fileRecordMapper.deleteById(id);
    }
}
