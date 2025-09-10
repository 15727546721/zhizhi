package cn.xu.domain.file.repository;

import cn.xu.domain.file.model.entity.FileEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件仓储接口
 * 定义文件实体的持久化操作
 */
public interface IFileRepository {
    
    /**
     * 保存文件实体
     * @param fileEntity 文件实体
     * @return 文件ID
     */
    String save(FileEntity fileEntity);
    
    /**
     * 批量保存文件实体
     * @param fileEntities 文件实体列表
     * @return 保存成功的数量
     */
    int batchSave(List<FileEntity> fileEntities);
    
    /**
     * 更新文件实体
     * @param fileEntity 文件实体
     * @return 更新成功数量
     */
    int update(FileEntity fileEntity);
    
    /**
     * 根据文件ID查询
     * @param fileId 文件ID
     * @return 文件实体
     */
    FileEntity findById(String fileId);
    
    /**
     * 根据文件URL查询
     * @param fileUrl 文件URL
     * @return 文件实体
     */
    FileEntity findByUrl(String fileUrl);
    
    /**
     * 根据用户ID查询文件列表
     * @param userId 用户ID
     * @param status 文件状态
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 文件实体列表
     */
    List<FileEntity> findByUserId(Long userId, FileEntity.FileStatus status, int offset, int limit);
    
    /**
     * 查询过期的临时文件
     * @param expirationTime 过期时间
     * @return 过期的临时文件列表
     */
    List<FileEntity> findExpiredTemporaryFiles(LocalDateTime expirationTime);
    
    /**
     * 统计用户文件数量
     * @param userId 用户ID
     * @param status 文件状态
     * @return 文件数量
     */
    long countByUserId(Long userId, FileEntity.FileStatus status);
    
    /**
     * 统计用户文件总大小
     * @param userId 用户ID
     * @param status 文件状态
     * @return 文件总大小（字节）
     */
    long sumFileSizeByUserId(Long userId, FileEntity.FileStatus status);
    
    /**
     * 根据文件状态删除文件
     * @param status 文件状态
     * @param beforeTime 删除此时间之前的文件
     * @return 删除的文件数量
     */
    int deleteByStatus(FileEntity.FileStatus status, LocalDateTime beforeTime);
}