package cn.xu.service.message;

import cn.xu.model.entity.UserBlock;
import cn.xu.repository.IUserBlockRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 用户屏蔽服务
 * 
 * <p>管理用户之间的屏蔽关系，独立于私信服务
 * <p>简化设计：直接使用PO，移除Entity转换
 *
 * @author xu
 * @since 2025-11-26
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserBlockService {

    private final IUserBlockRepository userBlockRepository;

    // ==================== 屏蔽管理 ====================

    /**
     * 屏蔽用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void blockUser(Long userId, Long blockedUserId) {
        if (userId == null || blockedUserId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (userId.equals(blockedUserId)) {
            throw new BusinessException("不能屏蔽自己");
        }

        // 幂等处理：如果已屏蔽，直接返回
        if (userBlockRepository.existsBlock(userId, blockedUserId)) {
            log.debug("[屏蔽] 用户已屏蔽 - 用户: {}, 被屏蔽: {}", userId, blockedUserId);
            return;
        }

        // 使用PO的工厂方法创建
        UserBlock block = UserBlock.create(userId, blockedUserId);
        userBlockRepository.save(block);
        log.info("[屏蔽] 屏蔽用户成功 - 用户: {}, 被屏蔽: {}", userId, blockedUserId);
    }

    /**
     * 取消屏蔽
     */
    @Transactional(rollbackFor = Exception.class)
    public void unblockUser(Long userId, Long blockedUserId) {
        if (userId == null || blockedUserId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        userBlockRepository.deleteByUserIdAndBlockedUserId(userId, blockedUserId);
        log.info("[屏蔽] 取消屏蔽成功 - 用户: {}, 被屏蔽: {}", userId, blockedUserId);
    }

    /**
     * 检查是否屏蔽
     */
    public boolean isBlocked(Long userId, Long blockedUserId) {
        if (userId == null || blockedUserId == null) {
            return false;
        }
        return userBlockRepository.existsBlock(userId, blockedUserId);
    }

    /**
     * 获取屏蔽列表
     */
    public List<UserBlock> getBlockList(Long userId, int pageNo, int pageSize) {
        if (userId == null) {
            return Collections.emptyList();
        }
        int offset = (pageNo - 1) * pageSize;
        return userBlockRepository.findByUserId(userId, offset, pageSize);
    }

    /**
     * 获取屏蔽数量
     */
    public int getBlockCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return userBlockRepository.countByUserId(userId);
    }
}
