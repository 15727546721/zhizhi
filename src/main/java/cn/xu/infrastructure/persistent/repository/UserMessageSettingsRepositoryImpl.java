package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.message.model.entity.UserMessageSettingsEntity;
import cn.xu.domain.message.repository.IUserMessageSettingsRepository;
import cn.xu.infrastructure.persistent.converter.UserMessageSettingsConverter;
import cn.xu.infrastructure.persistent.dao.UserMessageSettingsMapper;
import cn.xu.infrastructure.persistent.po.UserMessageSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户私信设置仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class UserMessageSettingsRepositoryImpl implements IUserMessageSettingsRepository {
    
    private final UserMessageSettingsMapper mapper;
    private final UserMessageSettingsConverter converter;
    
    @Override
    public void save(UserMessageSettingsEntity entity) {
        UserMessageSettings po = converter.toDataObject(entity);
        if (po.getId() == null) {
            mapper.insert(po);
            entity.setId(po.getId());
        } else {
            mapper.update(po);
        }
    }
    
    @Override
    public void update(UserMessageSettingsEntity entity) {
        UserMessageSettings po = converter.toDataObject(entity);
        mapper.update(po);
    }
    
    @Override
    public Optional<UserMessageSettingsEntity> findByUserId(Long userId) {
        UserMessageSettings po = mapper.selectByUserId(userId);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(converter.toDomainEntity(po));
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        mapper.deleteByUserId(userId);
    }
}

