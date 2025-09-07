package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Essay;
import cn.xu.infrastructure.persistent.po.EssayWithUserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 随笔数据访问接口
 * 负责随笔持久化对象的数据库操作
 */
@Mapper
public interface EssayMapper {
    
    /**
     * 保存随笔
     *
     * @param essay 随笔PO对象
     * @return 影响行数
     */
    Long insert(Essay essay);

    /**
     * 更新随笔
     *
     * @param essay 随笔PO对象
     * @return 影响行数
     */
    int update(Essay essay);

    /**
     * 根据ID删除随笔
     *
     * @param id 随笔ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除随笔
     *
     * @param ids 随笔ID列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID查询随笔
     *
     * @param id 随笔ID
     * @return 随笔PO
     */
    Essay findById(@Param("id") Long id);

    /**
     * 查询未分类的随笔
     *
     * @return 随笔PO列表
     */
    List<Essay> findWithoutCategory();

    /**
     * 获取随笔总数
     *
     * @return 随笔总数
     */
    Long count();

    /**
     * 根据条件查询随笔列表（包含用户信息）
     * 
     * @param offset 偏移量
     * @param size 每页大小
     * @param topic 话题关键词
     * @param essayType 随笔类型
     * @return 随笔与用户PO组合列表
     */
    List<EssayWithUserPO> findEssayList(@Param("offset") int offset,
                                        @Param("size") int size,
                                        @Param("topic") String topic,
                                        @Param("essayType") String essayType);

    /**
     * 更新评论数
     * 
     * @param targetId 目标随笔ID
     * @param count 变更数量（可为负数）
     */
    void updateCommentCount(@Param("targetId") Long targetId, @Param("count") int count);
    
    /**
     * 更新点赞数
     * 
     * @param essayId 随笔ID
     * @param count 变更数量（可为负数）
     */
    void updateLikeCount(@Param("essayId") Long essayId, @Param("count") int count);
    
    /**
     * 根据用户ID查询随笔列表
     * 
     * @param userId 用户ID
     * @param offset 偏移量
     * @param size 每页大小
     * @return 随笔PO列表
     */
    List<Essay> findByUserId(@Param("userId") Long userId, 
                            @Param("offset") int offset, 
                            @Param("size") int size);
    
    /**
     * 根据话题查询随笔
     * 
     * @param topic 话题名称
     * @param offset 偏移量
     * @param size 每页大小
     * @return 随笔PO列表
     */
    List<Essay> findByTopic(@Param("topic") String topic,
                           @Param("offset") int offset,
                           @Param("size") int size);
}