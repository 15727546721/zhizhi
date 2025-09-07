package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.entity.EssayWithUserAggregation;
import cn.xu.domain.essay.repository.IEssayRepository;
import cn.xu.infrastructure.persistent.converter.EssayConverter;
import cn.xu.infrastructure.persistent.dao.EssayMapper;
import cn.xu.infrastructure.persistent.po.Essay;
import cn.xu.infrastructure.persistent.po.EssayWithUserPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 随笔仓储实现
 * 负责随笔领域对象的持久化操作
 */
@Slf4j
@Repository
public class EssayRepository implements IEssayRepository {

    @Resource
    private EssayMapper essayMapper;
    
    @Resource
    private EssayConverter essayConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(EssayEntity essayEntity) {
        if (essayEntity == null) {
            throw new IllegalArgumentException("随笔实体不能为空");
        }
        
        try {
            // 转换为持久化对象
            Essay essay = essayConverter.toEssayPO(essayEntity);
            
            // 设置创建时间
            LocalDateTime now = LocalDateTime.now();
            essay.setCreateTime(now);
            essay.setUpdateTime(now);
            
            // 保存到数据库
            Long id = essayMapper.insert(essay);
            
            if (id == null || id <= 0) {
                log.error("保存随笔失败，返回ID无效: {}", id);
                throw new RuntimeException("保存随笔失败");
            }
            
            log.info("随笔保存成功，随笔ID: {}, 用户ID: {}", id, essayEntity.getUserId());
            return id;
            
        } catch (Exception e) {
            log.error("保存随笔异常，用户ID: {}", essayEntity.getUserId(), e);
            throw new RuntimeException("保存随笔失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(EssayEntity essayEntity) {
        if (essayEntity == null) {
            throw new IllegalArgumentException("随笔实体不能为空");
        }
        
        if (essayEntity.getId() == null || essayEntity.getId() <= 0) {
            throw new IllegalArgumentException("随笔ID不能为空");
        }
        
        try {
            // 转换为持久化对象
            Essay essay = essayConverter.toEssayPO(essayEntity);
            essay.setUpdateTime(LocalDateTime.now());
            
            int affectedRows = essayMapper.update(essay);
            if (affectedRows == 0) {
                log.warn("更新随笔失败，可能是随笔不存在，随笔ID: {}", essayEntity.getId());
                throw new RuntimeException("随笔不存在或更新失败");
            }
            
            log.info("随笔更新成功，随笔ID: {}", essayEntity.getId());
            
        } catch (Exception e) {
            log.error("更新随笔异常，随笔ID: {}", essayEntity.getId(), e);
            throw new RuntimeException("更新随笔失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("随笔ID不能为空");
        }
        
        try {
            int affectedRows = essayMapper.deleteById(id);
            if (affectedRows == 0) {
                log.warn("删除随笔失败，可能是随笔不存在，随笔ID: {}", id);
                // 这里不抛异常，因为删除不存在的记录可以认为是成功的
            }
            
            log.info("随笔删除成功，随笔ID: {}", id);
            
        } catch (Exception e) {
            log.error("删除随笔异常，随笔ID: {}", id, e);
            throw new RuntimeException("删除随笔失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("批量删除随笔，ID列表为空");
            return;
        }
        
        try {
            int affectedRows = essayMapper.deleteByIds(ids);
            log.info("批量删除随笔成功，删除数量: {}, 请求删除数量: {}", affectedRows, ids.size());
            
        } catch (Exception e) {
            log.error("批量删除随笔异常，ID列表: {}", ids, e);
            throw new RuntimeException("批量删除随笔失败: " + e.getMessage(), e);
        }
    }

    @Override
    public EssayEntity findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("随笔ID不能为空");
        }
        
        try {
            Essay essay = essayMapper.findById(id);
            EssayEntity entity = essayConverter.toEssayEntity(essay);
            
            if (entity != null) {
                log.debug("查询随笔成功，随笔ID: {}", id);
            }
            
            return entity;
            
        } catch (Exception e) {
            log.error("查询随笔异常，随笔ID: {}", id, e);
            throw new RuntimeException("查询随笔失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Long count() {
        try {
            Long count = essayMapper.count();
            log.debug("查询随笔总数成功: {}", count);
            return count != null ? count : 0L;
            
        } catch (Exception e) {
            log.error("查询随笔总数异常", e);
            // 返回0而不抛异常，避免影响业务流程
            return 0L;
        }
    }

    @Override
    public List<EssayWithUserAggregation> findEssayList(int page, int size, String topic, String essayType) {
        if (page <= 0 || size <= 0) {
            throw new IllegalArgumentException("分页参数无效");
        }
        
        try {
            int offset = (page - 1) * size;
            List<EssayWithUserPO> poList = essayMapper.findEssayList(offset, size, topic, essayType);
            
            // 转换为PO列表为领域聚合列表
            List<EssayWithUserAggregation> result = poList.stream()
                    .map(essayConverter::toEssayWithUserAggregation)
                    .collect(java.util.stream.Collectors.toList());
            
            log.debug("查询随笔列表成功，页码: {}, 大小: {}, 话题: {}, 类型: {}, 结果数量: {}", 
                    page, size, topic, essayType, result.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("查询随笔列表异常，页码: {}, 大小: {}, 话题: {}, 类型: {}", page, size, topic, essayType, e);
            throw new RuntimeException("查询随笔列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新评论数
     * 
     * @param essayId 随笔ID
     * @param count 变更数量
     */
    public void updateCommentCount(Long essayId, int count) {
        if (essayId == null || essayId <= 0) {
            throw new IllegalArgumentException("随笔ID不能为空");
        }
        
        try {
            essayMapper.updateCommentCount(essayId, count);
            log.info("更新随笔评论数成功，随笔ID: {}, 变更数量: {}", essayId, count);
            
        } catch (Exception e) {
            log.error("更新随笔评论数异常，随笔ID: {}, 变更数量: {}", essayId, count, e);
            throw new RuntimeException("更新评论数失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新点赞数
     * 
     * @param essayId 随笔ID
     * @param count 变更数量
     */
    public void updateLikeCount(Long essayId, int count) {
        if (essayId == null || essayId <= 0) {
            throw new IllegalArgumentException("随笔ID不能为空");
        }
        
        try {
            essayMapper.updateLikeCount(essayId, count);
            log.info("更新随笔点赞数成功，随笔ID: {}, 变更数量: {}", essayId, count);
            
        } catch (Exception e) {
            log.error("更新随笔点赞数异常，随笔ID: {}, 变更数量: {}", essayId, count, e);
            throw new RuntimeException("更新点赞数失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据用户ID查询随笔列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 随笔实体列表
     */
    public List<EssayEntity> findByUserId(Long userId, int page, int size) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        if (page <= 0 || size <= 0) {
            throw new IllegalArgumentException("分页参数无效");
        }
        
        try {
            int offset = (page - 1) * size;
            List<Essay> essays = essayMapper.findByUserId(userId, offset, size);
            
            List<EssayEntity> entities = essays.stream()
                    .map(essayConverter::toEssayEntity)
                    .collect(java.util.stream.Collectors.toList());
            
            log.debug("根据用户ID查询随笔成功，用户ID: {}, 结果数量: {}", userId, entities.size());
            return entities;
            
        } catch (Exception e) {
            log.error("根据用户ID查询随笔异常，用户ID: {}", userId, e);
            throw new RuntimeException("查询用户随笔失败: " + e.getMessage(), e);
        }
    }
} 