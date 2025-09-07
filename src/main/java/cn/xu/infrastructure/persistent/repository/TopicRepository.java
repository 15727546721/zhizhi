package cn.xu.infrastructure.persistent.repository;

import cn.xu.api.web.model.dto.essay.TopicQueryRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.essay.model.entity.TopicEntity;
import cn.xu.domain.essay.repository.ITopicRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.converter.TopicConverter;
import cn.xu.infrastructure.persistent.dao.TopicMapper;
import cn.xu.infrastructure.persistent.po.Topic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题仓储实现类
 * 通过TopicConverter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TopicRepository implements ITopicRepository {

    private final TopicMapper topicDao;
    private final TopicConverter topicConverter;

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public Long save(TopicEntity topic) {
        if (topic == null) {
            throw new IllegalArgumentException("话题实体不得为空");
        }
        try {
            Topic topicPO = topicConverter.toDataObject(topic);
            int rowsAffected = topicDao.insert(topicPO);
            if (rowsAffected <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存话题失败");
            }
            log.info("成功保存话题: {}", topic);
            return topicPO.getId();
        } catch (Exception e) {
            log.error("保存话题失败: {}", topic, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "保存话题失败");
        }
    }

    @Override
    @Transactional(rollbackFor = BusinessException.class)
    public void update(TopicEntity topicEntity) {
        if (topicEntity == null) {
            throw new IllegalArgumentException("话题实体不得为空");
        }
        try {
            Topic topicPO = topicConverter.toDataObject(topicEntity);
            int rowsAffected = topicDao.update(topicPO);
            if (rowsAffected <= 0) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新话题失败");
            }
            log.info("成功更新话题: {}", topicEntity);
        } catch (Exception e) {
            log.error("更新话题失败: {}", topicEntity, e);
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
            Topic topicPO = topicDao.findById(id);
            TopicEntity topicEntity = topicConverter.toDomainEntity(topicPO);
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
            List<Topic> topicPOs = topicDao.findAll();
            List<TopicEntity> topicEntities = topicConverter.toDomainEntities(topicPOs);
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
            TopicEntity topicEntity = topicConverter.toDomainEntity(topic);
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
        List<Topic> topicPOs = topicDao.getPageByName(request.getName(), offset, request.getPageSize());
        return topicConverter.toDomainEntities(topicPOs);
    }
}
