package cn.xu.domain.message.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.message.model.entity.UserBlockEntity;
import cn.xu.domain.message.repository.IUserBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户屏蔽领域服务
 * 负责处理用户屏蔽的核心业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBlockDomainService {
    
    private final IUserBlockRepository userBlockRepository;
    
    /**
     * 屏蔽用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void blockUser(Long userId, Long blockedUserId) {
        log.info("[用户屏蔽领域服务] 开始屏蔽用户 - 用户: {}, 被屏蔽用户: {}", userId, blockedUserId);
        
        try {
            // 验证参数
            validateBlockParams(userId, blockedUserId);
            
            // 检查是否已屏蔽
            if (userBlockRepository.existsBlock(userId, blockedUserId)) {
                log.info("[用户屏蔽领域服务] 用户已被屏蔽，无需重复屏蔽");
                return;
            }
            
            // 创建屏蔽关系
            UserBlockEntity userBlock = UserBlockEntity.create(userId, blockedUserId);
            userBlockRepository.save(userBlock);
            
            log.info("[用户屏蔽领域服务] 屏蔽用户成功 - 用户: {}, 被屏蔽用户: {}", userId, blockedUserId);
        } catch (Exception e) {
            log.error("[用户屏蔽领域服务] 屏蔽用户失败 - 用户: {}, 被屏蔽用户: {}", userId, blockedUserId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("屏蔽用户失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 取消屏蔽用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void unblockUser(Long userId, Long blockedUserId) {
        log.info("[用户屏蔽领域服务] 开始取消屏蔽用户 - 用户: {}, 被屏蔽用户: {}", userId, blockedUserId);
        
        try {
            // 验证参数
            validateBlockParams(userId, blockedUserId);
            
            // 查找屏蔽关系
            Optional<UserBlockEntity> userBlockOpt = userBlockRepository.findByUserAndBlockedUser(userId, blockedUserId);
            
            if (!userBlockOpt.isPresent()) {
                log.info("[用户屏蔽领域服务] 用户未被屏蔽，无需取消");
                return;
            }
            
            // 删除屏蔽关系
            UserBlockEntity userBlock = userBlockOpt.get();
            userBlockRepository.deleteById(userBlock.getId());
            
            log.info("[用户屏蔽领域服务] 取消屏蔽用户成功 - 用户: {}, 被屏蔽用户: {}", userId, blockedUserId);
        } catch (Exception e) {
            log.error("[用户屏蔽领域服务] 取消屏蔽用户失败 - 用户: {}, 被屏蔽用户: {}", userId, blockedUserId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("取消屏蔽用户失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 检查用户是否被屏蔽
     */
    public boolean isBlocked(Long userId, Long blockedUserId) {
        return userBlockRepository.existsBlock(userId, blockedUserId);
    }
    
    /**
     * 获取用户的屏蔽列表
     */
    public List<UserBlockEntity> getBlockList(Long userId, Integer pageNo, Integer pageSize) {
        try {
            int offset = (pageNo - 1) * pageSize;
            return userBlockRepository.findByUserId(userId, offset, pageSize);
        } catch (Exception e) {
            log.error("[用户屏蔽领域服务] 获取屏蔽列表失败 - 用户: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户的屏蔽数量
     */
    public int getBlockCount(Long userId) {
        try {
            return userBlockRepository.countByUserId(userId);
        } catch (Exception e) {
            log.error("[用户屏蔽领域服务] 获取屏蔽数量失败 - 用户: {}", userId, e);
            return 0;
        }
    }
    
    /**
     * 验证屏蔽参数
     */
    private void validateBlockParams(Long userId, Long blockedUserId) {
        UserBlockEntity.validateBlockRelation(userId, blockedUserId);
    }
}

