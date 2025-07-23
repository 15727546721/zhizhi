package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.entity.EssayWithUserAggregation;
import cn.xu.infrastructure.persistent.po.Essay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 话题数据访问接口
 */
@Mapper
public interface EssayMapper {
    /**
     * 保存随笔
     *
     * @param essayEntity
     * @return 影响行数
     */
    Long insert(EssayEntity essayEntity);

    /**
     * 更新随笔
     *
     * @param essay
     * @return 影响行数
     */
    int update(EssayEntity essay);

    /**
     * 根据ID删除话题
     *
     * @param id 话题ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除话题
     *
     * @param ids 话题ID列表
     * @return 影响行数
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID查询话题
     *
     * @param id 话题ID
     * @return 话题PO
     */
    Essay findById(@Param("id") Long id);

    /**
     * 查询未分类的话题
     *
     * @return 话题PO列表
     */
    List<Essay> findWithoutCategory();

    /**
     * 获取话题总数
     *
     * @return 话题总数
     */
    Long count();

    /**
     * 根据话题ID查询话题详情
     * @param offset
     * @param size
     * @param topic
     * @param essayType
     * @return
     */
    List<EssayWithUserAggregation> findEssayList(@Param("offset") int offset,
                                                 @Param("size") int size,
                                                 @Param("topic") String topic,
                                                 @Param("essayType") String essayType);

    /**
     * 更新评论数
     */
    void updateCommentCount(@Param("targetId") Long targetId, @Param("count") int count);
}