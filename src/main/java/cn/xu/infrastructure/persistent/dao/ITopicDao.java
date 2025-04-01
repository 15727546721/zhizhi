package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.essay.model.entity.TopicEntity;
import cn.xu.infrastructure.persistent.po.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 话题分类数据访问接口
 */
@Mapper
public interface ITopicDao {
    /**
     * 插入分类
     *
     * @param categoryPO 分类PO
     * @return 影响行数
     */
    int insert(Topic categoryPO);

    /**
     * 更新分类
     *
     * @param categoryPO 分类PO
     * @return 影响行数
     */
    int update(Topic categoryPO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类PO
     */
    Topic findById(@Param("id") Long id);

    /**
     * 查询所有分类
     *
     * @return 分类PO列表
     */
    List<Topic> findAll();

    /**
     * 根据名称查询分类
     *
     * @param name 分类名称
     * @return 分类PO
     */
    Topic findByName(@Param("name") String name);

    /**
     * 根据名称分页查询分类
     *
     * @param name
     * @param offset
     * @param pageSize
     * @return
     */
    List<TopicEntity> getPageByName(@Param("name") String name,
                                    @Param("offset") int offset,
                                    @Param("pageSize") Integer pageSize);
}