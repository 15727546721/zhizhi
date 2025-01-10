package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.topic.model.entity.TopicEntity;
import cn.xu.domain.topic.repository.ITopicRepository;
import cn.xu.infrastructure.persistent.dao.ITopicDao;
import cn.xu.infrastructure.persistent.po.TopicPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TopicRepository implements ITopicRepository {

    @Resource
    private ITopicDao topicDao;

    @Override
    public Long save(TopicEntity topicEntity) {
        TopicPO topicPO = convertToTopicPO(topicEntity);
        topicDao.insert(topicPO);
        return topicPO.getId();
    }

    @Override
    public void update(TopicEntity topicEntity) {
        topicDao.update(convertToTopicPO(topicEntity));
    }

    @Override
    public void deleteById(Long id) {
        topicDao.deleteById(id);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        topicDao.deleteByIds(ids);
    }

    @Override
    public TopicEntity findById(Long id) {
        TopicPO topicPO = topicDao.findById(id);
        return topicPO != null ? convertToTopicEntity(topicPO) : null;
    }

    @Override
    public List<TopicEntity> findAll() {
        try {
            log.info("查询所有话题列表");
            List<TopicPO> topicPOs = topicDao.findAll();
            if (topicPOs == null || topicPOs.isEmpty()) {
                return Collections.emptyList();
            }
            return topicPOs.stream()
                    .map(this::convertToTopicEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询所有话题列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<TopicEntity> findHotTopics(int limit) {
        return topicDao.findHotTopics(limit).stream()
                .map(this::convertToTopicEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopicEntity> findByCategoryId(Long categoryId) {
        return topicDao.findByCategoryId(categoryId).stream()
                .map(this::convertToTopicEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<TopicEntity> findByPage(int offset, int limit) {
        try {
            log.info("分页查询话题列表, offset: {}, limit: {}", offset, limit);
            List<TopicPO> topicPOs = topicDao.findByPage(offset, limit);
            if (topicPOs == null || topicPOs.isEmpty()) {
                return Collections.emptyList();
            }
            return topicPOs.stream()
                    .map(this::convertToTopicEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分页查询话题列表失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Long count() {
        try {
            return topicDao.count();
        } catch (Exception e) {
            log.error("查询话题总数失败", e);
            return 0L;
        }
    }

    /**
     * 将领域实体转换为PO对象
     */
    private TopicPO convertToTopicPO(TopicEntity topicEntity) {
        if (topicEntity == null) {
            return null;
        }

        TopicPO topicPO = TopicPO.builder()
                .id(topicEntity.getId())
                .userId(topicEntity.getUserId())
                .content(topicEntity.getContent())
                .categoryId(topicEntity.getCategoryId())
                .createTime(topicEntity.getCreateTime())
                .updateTime(topicEntity.getUpdateTime())
                .build();

        // 设置图片列表
        if (topicEntity.getImages() != null && !topicEntity.getImages().isEmpty()) {
            topicPO.setImageList(topicEntity.getImages());
        }
        return topicPO;
    }

    /**
     * 将PO对象转换为领域实体
     */
    private TopicEntity convertToTopicEntity(TopicPO topicPO) {
        if (topicPO == null) {
            return null;
        }

        return TopicEntity.builder()
                .id(topicPO.getId())
                .userId(topicPO.getUserId())
                .content(topicPO.getContent())
                .images(topicPO.getImageList())  // 使用getImageList获取图片列表
                .categoryId(topicPO.getCategoryId())
                .createTime(topicPO.getCreateTime())
                .updateTime(topicPO.getUpdateTime())
                .build();
    }
} 