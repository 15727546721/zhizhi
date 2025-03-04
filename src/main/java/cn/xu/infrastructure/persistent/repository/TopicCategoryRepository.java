package cn.xu.infrastructure.persistent.repository;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.topic.model.entity.TopicCategoryEntity;
import cn.xu.domain.topic.repository.ITopicCategoryRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.dao.ITopicCategoryDao;
import cn.xu.infrastructure.persistent.po.TopicCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TopicCategoryRepository implements ITopicCategoryRepository {

    @Resource
    private ITopicCategoryDao topicCategoryDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(TopicCategoryEntity categoryEntity) {
        try {
            log.info("保存话题分类: {}", categoryEntity);
            TopicCategory categoryPO = convertToTopicCategoryPO(categoryEntity);
            int rows = topicCategoryDao.insert(categoryPO);
            if (rows <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存话题分类失败");
            }
            return categoryPO.getId();
        } catch (Exception e) {
            log.error("保存话题分类失败: {}", categoryEntity, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存话题分类失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TopicCategoryEntity categoryEntity) {
        try {
            log.info("更新话题分类: {}", categoryEntity);
            TopicCategory categoryPO = convertToTopicCategoryPO(categoryEntity);
            int rows = topicCategoryDao.update(categoryPO);
            if (rows <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新话题分类失败");
            }
        } catch (Exception e) {
            log.error("更新话题分类失败: {}", categoryEntity, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新话题分类失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        try {
            log.info("删除话题分类: {}", id);
            int rows = topicCategoryDao.deleteById(id);
            if (rows <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除话题分类失败");
            }
        } catch (Exception e) {
            log.error("删除话题分类失败: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除话题分类失败");
        }
    }

    @Override
    public TopicCategoryEntity findById(Long id) {
        try {
            log.info("查询话题分类: {}", id);
            TopicCategory categoryPO = topicCategoryDao.findById(id);
            return categoryPO != null ? convertToTopicCategoryEntity(categoryPO) : null;
        } catch (Exception e) {
            log.error("查询话题分类失败: {}", id, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询话题分类失败");
        }
    }

    @Override
    public List<TopicCategoryEntity> findAll() {
        try {
            log.info("查询所有话题分类");
            List<TopicCategory> categoryPOs = topicCategoryDao.findAll();
            if (categoryPOs == null || categoryPOs.isEmpty()) {
                return Collections.emptyList();
            }
            return categoryPOs.stream()
                    .map(this::convertToTopicCategoryEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询所有话题分类失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询所有话题分类失败");
        }
    }

    @Override
    public TopicCategoryEntity findByName(String name) {
        try {
            log.info("根据名称查询话题分类: {}", name);
            TopicCategory categoryPO = topicCategoryDao.findByName(name);
            return categoryPO != null ? convertToTopicCategoryEntity(categoryPO) : null;
        } catch (Exception e) {
            log.error("根据名称查询话题分类失败: {}", name, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "根据名称查询话题分类失败");
        }
    }

    /**
     * 将领域实体转换为PO对象
     */
    private TopicCategory convertToTopicCategoryPO(TopicCategoryEntity categoryEntity) {
        if (categoryEntity == null) {
            return null;
        }
        return TopicCategory.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .description(categoryEntity.getDescription())
                .sort(categoryEntity.getSort())
                .createTime(categoryEntity.getCreateTime())
                .updateTime(categoryEntity.getUpdateTime())
                .build();
    }

    /**
     * 将PO对象转换为领域实体
     */
    private TopicCategoryEntity convertToTopicCategoryEntity(TopicCategory categoryPO) {
        if (categoryPO == null) {
            return null;
        }
        return TopicCategoryEntity.builder()
                .id(categoryPO.getId())
                .name(categoryPO.getName())
                .description(categoryPO.getDescription())
                .sort(categoryPO.getSort())
                .createTime(categoryPO.getCreateTime())
                .updateTime(categoryPO.getUpdateTime())
                .build();
    }
} 