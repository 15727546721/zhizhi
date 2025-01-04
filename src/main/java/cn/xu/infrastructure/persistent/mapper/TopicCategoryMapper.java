package cn.xu.infrastructure.persistent.mapper;

import cn.xu.domain.topic.entity.TopicCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TopicCategoryMapper {
    void insert(TopicCategory category);

    TopicCategory findById(@Param("id") Long id);

    List<TopicCategory> findAll();

    List<TopicCategory> findAllOrderBySort();

    void delete(@Param("id") Long id);
} 