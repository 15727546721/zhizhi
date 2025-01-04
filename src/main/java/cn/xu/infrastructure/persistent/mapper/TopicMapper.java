package cn.xu.infrastructure.persistent.mapper;

import cn.xu.domain.topic.entity.Topic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TopicMapper {
    void insert(Topic topic);

    Topic findById(@Param("id") Long id);

    List<Topic> findByUserId(@Param("userId") Long userId);

    List<Topic> findByCategoryId(@Param("categoryId") Long categoryId);

    List<Topic> findAll(@Param("offset") int offset, @Param("size") int size);

    void delete(@Param("id") Long id);
} 