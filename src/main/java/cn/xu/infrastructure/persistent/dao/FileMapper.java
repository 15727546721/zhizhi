package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件数据访问层映射器
 */
@Mapper
public interface FileMapper {
    
    /**
     * 插入文件记录
     * @param file 文件PO对象
     * @return 影响行数
     */
    int insert(File file);
    
    /**
     * 批量插入文件记录
     * @param files 文件PO对象列表
     * @return 影响行数
     */
    int batchInsert(@Param("files") List<File> files);
    
    /**
     * 更新文件记录
     * @param file 文件PO对象
     * @return 影响行数
     */
    int update(File file);
    
    /**
     * 根据文件ID查询
     * @param fileId 文件ID
     * @return 文件PO对象
     */
    File selectById(@Param("fileId") String fileId);
    
    /**
     * 根据文件URL查询
     * @param fileUrl 文件URL
     * @return 文件PO对象
     */
    File selectByUrl(@Param("fileUrl") String fileUrl);
    
    /**
     * 根据用户ID和状态查询文件列表
     * @param userId 用户ID
     * @param status 文件状态
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 文件PO对象列表
     */
    List<File> selectByUserIdAndStatus(@Param("userId") Long userId, 
                                      @Param("status") String status, 
                                      @Param("offset") int offset, 
                                      @Param("limit") int limit);
    
    /**
     * 查询过期的临时文件
     * @param expirationTime 过期时间
     * @return 过期的临时文件列表
     */
    List<File> selectExpiredTemporaryFiles(@Param("expirationTime") LocalDateTime expirationTime);
    
    /**
     * 统计用户文件数量
     * @param userId 用户ID
     * @param status 文件状态
     * @return 文件数量
     */
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * 统计用户文件总大小
     * @param userId 用户ID
     * @param status 文件状态
     * @return 文件总大小（字节）
     */
    long sumFileSizeByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * 根据文件状态和时间删除文件
     * @param status 文件状态
     * @param beforeTime 删除此时间之前的文件
     * @return 删除的文件数量
     */
    int deleteByStatusAndTime(@Param("status") String status, @Param("beforeTime") LocalDateTime beforeTime);
}