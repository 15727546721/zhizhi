package cn.xu.repository.impl;

import cn.xu.model.entity.UserBlock;
import cn.xu.repository.IUserBlockRepository;
import cn.xu.repository.mapper.UserBlockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 用户屏蔽仓储实现
 * <p>负责用户屏蔽关系的持久化操作</p>

 */
@Repository
@RequiredArgsConstructor
public class UserBlockRepository implements IUserBlockRepository {
    
    private final UserBlockMapper userBlockMapper;
    
    @Override
    public Long save(UserBlock userBlock) {
        if (userBlock.getId() == null) {
            userBlockMapper.insert(userBlock);
            return userBlock.getId();
        }
        // UserBlock只有插入和删除，没有更新操作
        return userBlock.getId();
    }
    
    @Override
    public void deleteById(Long id) {
        userBlockMapper.deleteById(id);
    }
    
    @Override
    public Optional<UserBlock> findById(Long id) {
        UserBlock po = userBlockMapper.selectById(id);
        return Optional.ofNullable(po);
    }
    
    @Override
    public boolean existsBlock(Long userId, Long blockedUserId) {
        return userBlockMapper.existsBlock(userId, blockedUserId);
    }
    
    @Override
    public Optional<UserBlock> findByUserAndBlockedUser(Long userId, Long blockedUserId) {
        UserBlock po = userBlockMapper.selectByUserAndBlockedUser(userId, blockedUserId);
        return Optional.ofNullable(po);
    }
    
    @Override
    public List<UserBlock> findByUserId(Long userId, Integer offset, Integer limit) {
        List<UserBlock> userBlocks = userBlockMapper.selectByUserId(userId, offset, limit);
        return userBlocks != null ? userBlocks : Collections.emptyList();
    }
    
    @Override
    public int countByUserId(Long userId) {
        return userBlockMapper.countByUserId(userId);
    }
    
    @Override
    public void deleteByUserIdAndBlockedUserId(Long userId, Long blockedUserId) {
        userBlockMapper.deleteByUserAndBlockedUser(userId, blockedUserId);
    }
}

