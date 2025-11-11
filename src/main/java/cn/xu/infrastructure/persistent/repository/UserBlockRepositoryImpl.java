package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.message.model.entity.UserBlockEntity;
import cn.xu.domain.message.repository.IUserBlockRepository;
import cn.xu.infrastructure.persistent.converter.UserBlockConverter;
import cn.xu.infrastructure.persistent.dao.UserBlockMapper;
import cn.xu.infrastructure.persistent.po.UserBlock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户屏蔽仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class UserBlockRepositoryImpl implements IUserBlockRepository {
    
    private final UserBlockMapper userBlockMapper;
    private final UserBlockConverter userBlockConverter;
    
    @Override
    public Long save(UserBlockEntity userBlock) {
        UserBlock po = userBlockConverter.toDataObject(userBlock);
        if (po.getId() == null) {
            userBlockMapper.insert(po);
            userBlock.setId(po.getId());
            return po.getId();
        } else {
            // UserBlock只有插入和删除，没有更新操作
            return po.getId();
        }
    }
    
    @Override
    public void deleteById(Long id) {
        userBlockMapper.deleteById(id);
    }
    
    @Override
    public Optional<UserBlockEntity> findById(Long id) {
        UserBlock po = userBlockMapper.selectById(id);
        return Optional.ofNullable(userBlockConverter.toDomainEntity(po));
    }
    
    @Override
    public boolean existsBlock(Long userId, Long blockedUserId) {
        return userBlockMapper.existsBlock(userId, blockedUserId);
    }
    
    @Override
    public Optional<UserBlockEntity> findByUserAndBlockedUser(Long userId, Long blockedUserId) {
        UserBlock po = userBlockMapper.selectByUserAndBlockedUser(userId, blockedUserId);
        return Optional.ofNullable(userBlockConverter.toDomainEntity(po));
    }
    
    @Override
    public List<UserBlockEntity> findByUserId(Long userId, Integer offset, Integer limit) {
        List<UserBlock> userBlocks = userBlockMapper.selectByUserId(userId, offset, limit);
        return userBlockConverter.toDomainEntities(userBlocks);
    }
    
    @Override
    public int countByUserId(Long userId) {
        return userBlockMapper.countByUserId(userId);
    }
}

