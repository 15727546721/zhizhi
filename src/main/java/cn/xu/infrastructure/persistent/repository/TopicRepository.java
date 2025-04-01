package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.web.model.dto.essay.TopicQueryRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.essay.model.entity.TopicEntity;
import cn.xu.domain.essay.repository.ITopicRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.dao.ITopicDao;
import cn.xu.infrastructure.persistent.po.Topic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TopicRepository implements ITopicRepository {

    private final ITopicDao topicDao;

    @Autowired
    public TopicRepository(ITopicDao topicDao) {
        this.topicDao = topicDao;
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public Long save(TopicEntity topic) {
        if (topic == null) {
            throw new IllegalArgumentException("话题实体不得为空");
        }
        try {
            Topic categoryPO = convertEntityToPO(topic);
            int rowsAffected = topicDao.insert(categoryPO);
            if (rowsAffected <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存话题失败");
            }
            log.info("成功保存话题分类: {}", topic);
            return categoryPO.getId();
        } catch (Exception e) {
            log.error("保存话题失败: {}", topic, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存话题失败");
        }
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public void update(TopicEntity categoryEntity) {
        if (categoryEntity == null) {
            throw new IllegalArgumentException("话题实体不得为空");
        }
        try {
            Topic categoryPO = convertEntityToPO(categoryEntity);
            int rowsAffected = topicDao.update(categoryPO);
            if (rowsAffected <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新话题失败");
            }
            log.info("成功更新话题分类: {}", categoryEntity);
        } catch (Exception e) {
            log.error("更新话题分类失败: {}", categoryEntity, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新话题失败");
        }
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("话题ID不得为空");
        }
        try {
            int rowsAffected = topicDao.deleteById(id);
            if (rowsAffected <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除话题失败");
            }
            log.info("成功删除话题: {}", id);
        } catch (Exception e) {
            log.error("删除话题失败: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除话题失败");
        }
    }

    @Override
    public TopicEntity findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("话题ID不得为空");
        }
        try {
            Topic categoryPO = topicDao.findById(id);
            TopicEntity topicEntity = categoryPO != null ? convertPOToEntity(categoryPO) : null;
            log.info("查询话题结果: {}", topicEntity);
            return topicEntity;
        } catch (Exception e) {
            log.error("查询话题失败: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询话题失败");
        }
    }

    @Override
    public List<TopicEntity> findAll() {
        try {
            List<Topic> categoryPOs = topicDao.findAll();
            List<TopicEntity> topicEntities = categoryPOs.stream()
                    .map(this::convertPOToEntity)
                    .collect(Collectors.toList());
            log.info("查询所有话题结果: {}", topicEntities);
            return topicEntities.isEmpty() ? Collections.emptyList() : topicEntities;
        } catch (Exception e) {
            log.error("查询所有话题失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询所有话题失败");
        }
    }

    @Override
    public TopicEntity findByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("话题名称不得为空");
        }
        try {
            Topic topic = topicDao.findByName(name);
            TopicEntity topicEntity = topic != null ? convertPOToEntity(topic) : null;
            log.info("根据名称查询话题结果: {}", topicEntity);
            return topicEntity;
        } catch (Exception e) {
            log.error("根据名称查询话题失败: {}", name, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "根据名称查询话题失败");
        }
    }

    @Override
    public List<TopicEntity> getPageByName(TopicQueryRequest request) {
        int offset = (request.getPageNo() - 1) * request.getPageSize();
        List<TopicEntity> topicEntities = topicDao.getPageByName(request.getName(), offset, request.getPageSize());
        return topicEntities;
    }

    /**
     * 将领域实体转换为PO对象
     */
    private Topic convertEntityToPO(TopicEntity categoryEntity) {
        return Topic.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .createTime(categoryEntity.getCreateTime())
                .updateTime(categoryEntity.getUpdateTime())
                .build();
    }

    /**
     * 将PO对象转换为领域实体
     */
    private TopicEntity convertPOToEntity(Topic categoryPO) {
        return TopicEntity.builder()
                .id(categoryPO.getId())
                .name(categoryPO.getName())
                .createTime(categoryPO.getCreateTime())
                .updateTime(categoryPO.getUpdateTime())
                .build();
    }
}
