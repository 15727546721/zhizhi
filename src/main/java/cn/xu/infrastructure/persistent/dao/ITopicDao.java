package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.TopicPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 话题数据访问接口
 */
@Mapper
public interface ITopicDao {
    /**
     * 插入话题
     *
     * @param topicPO 话题PO
     * @return 影响行数
     */
    int insert(TopicPO topicPO);

    /**
     * 更新话题
     *
     * @param topicPO 话题PO
     * @return 影响行数
     */
    int update(TopicPO topicPO);

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
    TopicPO findById(@Param("id") Long id);

    /**
     * 查询所有话题
     *
     * @return 话题PO列表
     */
    List<TopicPO> findAll();

    /**
     * 查询热门话题
     *
     * @param limit 限制数量
     * @return 话题PO列表
     */
    List<TopicPO> findHotTopics(@Param("limit") int limit);

    /**
     * 根据分类ID查询话题列表
     *
     * @param categoryId 分类ID
     * @return 话题PO列表
     */
    List<TopicPO> findByCategoryId(@Param("categoryId") Long categoryId);
} 