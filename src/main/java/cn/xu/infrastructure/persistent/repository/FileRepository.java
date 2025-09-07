package cn.xu.infrastructure.persistent.repository;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.file.model.entity.FileEntity;
import cn.xu.domain.file.repository.IFileRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.converter.FileConverter;
import cn.xu.infrastructure.persistent.dao.FileMapper;
import cn.xu.infrastructure.persistent.po.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件仓储实现类
 * 通过FileConverter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FileRepository implements IFileRepository {
    
    private final FileMapper fileMapper;
    private final FileConverter fileConverter;
    
    @Override
    public String save(FileEntity fileEntity) {
        if (fileEntity == null) {
            throw new IllegalArgumentException("文件实体不能为空");
        }
        
        try {
            File filePO = fileConverter.toDataObject(fileEntity);
            int result = fileMapper.insert(filePO);
            if (result <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存文件失败");
            }
            log.info("成功保存文件: {}", fileEntity.getFileId());
            return fileEntity.getFileId();
        } catch (Exception e) {
            log.error("保存文件失败: {}", fileEntity.getFileId(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public int batchSave(List<FileEntity> fileEntities) {
        if (fileEntities == null || fileEntities.isEmpty()) {
            return 0;
        }
        
        try {
            List<File> filePOs = fileConverter.toDataObjects(fileEntities);
            
            int result = fileMapper.batchInsert(filePOs);
            log.info("批量保存文件成功，数量: {}", result);
            return result;
        } catch (Exception e) {
            log.error("批量保存文件失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量保存文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public int update(FileEntity fileEntity) {
        if (fileEntity == null) {
            throw new IllegalArgumentException("文件实体不能为空");
        }
        
        try {
            File filePO = fileConverter.toDataObject(fileEntity);
            int result = fileMapper.update(filePO);
            log.info("更新文件成功: {}", fileEntity.getFileId());
            return result;
        } catch (Exception e) {
            log.error("更新文件失败: {}", fileEntity.getFileId(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public FileEntity findById(String fileId) {
        try {
            File filePO = fileMapper.selectById(fileId);
            return fileConverter.toDomainEntity(filePO);
        } catch (Exception e) {
            log.error("根据ID查询文件失败: {}", fileId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public FileEntity findByUrl(String fileUrl) {
        try {
            File filePO = fileMapper.selectByUrl(fileUrl);
            return fileConverter.toDomainEntity(filePO);
        } catch (Exception e) {
            log.error("根据URL查询文件失败: {}", fileUrl, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<FileEntity> findByUserId(Long userId, FileEntity.FileStatus status, int offset, int limit) {
        try {
            String statusStr = status != null ? status.name() : null;
            List<File> filePOs = fileMapper.selectByUserIdAndStatus(userId, statusStr, offset, limit);
            return fileConverter.toDomainEntities(filePOs);
        } catch (Exception e) {
            log.error("根据用户ID查询文件失败: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<FileEntity> findExpiredTemporaryFiles(LocalDateTime expirationTime) {
        try {
            List<File> filePOs = fileMapper.selectExpiredTemporaryFiles(expirationTime);
            return fileConverter.toDomainEntities(filePOs);
        } catch (Exception e) {
            log.error("查询过期临时文件失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询过期文件失败: " + e.getMessage());
        }
    }
    
    @Override
    public long countByUserId(Long userId, FileEntity.FileStatus status) {
        try {
            String statusStr = status != null ? status.name() : null;
            return fileMapper.countByUserIdAndStatus(userId, statusStr);
        } catch (Exception e) {
            log.error("统计用户文件数量失败: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "统计文件数量失败: " + e.getMessage());
        }
    }
    
    @Override
    public long sumFileSizeByUserId(Long userId, FileEntity.FileStatus status) {
        try {
            String statusStr = status != null ? status.name() : null;
            return fileMapper.sumFileSizeByUserIdAndStatus(userId, statusStr);
        } catch (Exception e) {
            log.error("统计用户文件大小失败: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "统计文件大小失败: " + e.getMessage());
        }
    }
    
    @Override
    public int deleteByStatus(FileEntity.FileStatus status, LocalDateTime beforeTime) {
        try {
            String statusStr = status.name();
            int result = fileMapper.deleteByStatusAndTime(statusStr, beforeTime);
            log.info("删除文件成功，状态: {}, 删除数量: {}", status, result);
            return result;
        } catch (Exception e) {
            log.error("根据状态删除文件失败: {}", status, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除文件失败: " + e.getMessage());
        }
    }
    
}