package cn.xu.repository.mapper;

import cn.xu.model.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件记录Mapper
 */
@Mapper
public interface FileRecordMapper {
    
    /**
     * 插入文件记录
     */
    int insert(FileRecord record);
    
    /**
     * 根据ID查询
     */
    FileRecord findById(@Param("id") Long id);
    
    /**
     * 根据存储路径查询（V2.0）
     */
    FileRecord findByStoragePath(@Param("storagePath") String storagePath);
    
    /**
     * 根据文件名查询
     */
    FileRecord findByFileName(@Param("fileName") String fileName);
    
    /**
     * 查询用户上传的文件
     */
    List<FileRecord> findByUserId(@Param("userId") Long userId, 
                                   @Param("status") Integer status);
    
    
    /**
     * 查询指定时间之前的临时文件
     */
    List<FileRecord> findTemporaryFilesBefore(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 更新文件记录
     */
    int update(FileRecord record);
    
    /**
     * 更新文件状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    
    /**
     * 批量删除（逻辑删除）
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
    
    /**
     * 物理删除（谨慎使用）
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 统计用户文件数量
     */
    Long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);
}
