package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 话题数据访问接口
 */
@Mapper
public interface TopicMapper {
    /**
     * 添加话题
     *
     * @param name 话题名称
     */
    void addTopic(@Param("name") String name);
    
    /**
     * 根据ID获取话题
     *
     * @param id 话题ID
     * @return 话题PO
     */
    Topic getTopicById(@Param("id") Long id);
    
    /**
     * 获取所有话题
     *
     * @return 话题PO列表
     */
    List<Topic> getAllTopics();
    
    /**
     * 搜索话题
     *
     * @param keyword 搜索关键词
     * @return 话题PO列表
     */
    List<Topic> searchTopics(@Param("keyword") String keyword);
    
    /**
     * 获取热门话题
     *
     * @param limit 限制数量
     * @return 话题PO列表
     */
    List<Topic> getHotTopics(@Param("limit") int limit);
    
    /**
     * 根据名称查询话题
     *
     * @param name 话题名称
     * @return 话题PO
     */
    Topic findByName(@Param("name") String name);
    
    /**
     * 根据ID列表批量查询话题
     *
     * @param ids 话题ID列表
     * @return 话题PO列表
     */
    List<Topic> selectByIds(@Param("ids") List<Long> ids);
}