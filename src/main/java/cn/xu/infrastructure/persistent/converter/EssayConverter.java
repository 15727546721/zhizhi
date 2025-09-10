package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.essay.model.entity.EssayEntity;
import cn.xu.domain.essay.model.entity.EssayWithUserAggregation;
import cn.xu.infrastructure.persistent.po.Essay;
import cn.xu.infrastructure.persistent.po.EssayWithUserPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 随笔转换器
 * 负责领域实体与持久化对象之间的转换
 * 
 * @author xu
 */
@Slf4j
@Component
public class EssayConverter {
    
    @Resource
    private UserConverter userConverter;
    
    /**
     * 将领域实体转换为持久化对象
     * 
     * @param entity 领域实体
     * @return 持久化对象
     */
    public Essay toEssayPO(EssayEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Essay.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .content(entity.getContentString())
                .images(entity.getImagesString())
                .topics(entity.getTopicsString())
                .likeCount(entity.getLikeCount())
                .commentCount(entity.getCommentCount())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
    
    /**
     * 将持久化对象转换为领域实体
     * 
     * @param po 持久化对象
     * @return 领域实体
     */
    public EssayEntity toEssayEntity(Essay po) {
        if (po == null) {
            return null;
        }
        
        return EssayEntity.restore(
                po.getId(),
                po.getUserId(),
                po.getContent(),
                po.getImages(),
                po.getTopics(),
                po.getLikeCount(),
                po.getCommentCount(),
                po.getCreateTime(),
                po.getUpdateTime()
        );
    }
    
    /**
     * 将包含用户信息的持久化对象转换为领域聚合
     * 
     * @param po 包含用户信息的持久化对象
     * @return 领域聚合对象
     */
    public EssayWithUserAggregation toEssayWithUserAggregation(EssayWithUserPO po) {
        if (po == null) {
            return null;
        }
        
        EssayWithUserAggregation aggregation = new EssayWithUserAggregation();
        aggregation.setId(po.getId());
        aggregation.setContent(po.getContent());
        aggregation.setImages(po.getImages());
        aggregation.setTopics(po.getTopics());
        aggregation.setLikeCount(po.getLikeCount());
        aggregation.setCommentCount(po.getCommentCount());
        aggregation.setCreateTime(po.getCreateTime());
        aggregation.setUpdateTime(po.getUpdateTime());
        
        // 转换用户信息
        if (po.getUser() != null) {
            aggregation.setUser(userConverter.toDomainEntity(po.getUser()));
        }
        
        return aggregation;
    }
}