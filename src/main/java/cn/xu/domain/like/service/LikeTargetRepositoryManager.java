package cn.xu.domain.like.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.repository.LikeTargetRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 点赞目标仓储管理器
 * 根据点赞类型获取对应的仓储策略，消除硬编码
 */
@Slf4j
@Component
public class LikeTargetRepositoryManager {
    
    private final List<LikeTargetRepository> repositories;
    
    @Autowired
    public LikeTargetRepositoryManager(List<LikeTargetRepository> repositories) {
        this.repositories = repositories;
        log.info("[点赞目标仓储管理器] 初始化完成，共加载 {} 个仓储策略", repositories.size());
    }
    
    /**
     * 根据点赞类型获取对应的仓储
     * 
     * @param likeType 点赞类型
     * @return 对应的仓储策略
     */
    public LikeTargetRepository getRepository(LikeType likeType) {
        if (likeType == null) {
            throw new BusinessException("点赞类型不能为空");
        }
        
        return repositories.stream()
                .filter(repo -> repo.supports(likeType))
                .findFirst()
                .orElseThrow(() -> new BusinessException("不支持的点赞类型: " + likeType));
    }
    
    /**
     * 更新目标的点赞数
     * 
     * @param likeType 点赞类型
     * @param targetId 目标ID
     * @param increment 增量值
     */
    public void updateLikeCount(LikeType likeType, Long targetId, Long increment) {
        LikeTargetRepository repository = getRepository(likeType);
        repository.updateLikeCount(targetId, increment);
    }
}

