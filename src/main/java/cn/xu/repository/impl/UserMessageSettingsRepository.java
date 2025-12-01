package cn.xu.repository.impl;

import cn.xu.model.entity.UserMessageSettings;
import cn.xu.repository.IUserMessageSettingsRepository;
import cn.xu.repository.mapper.UserMessageSettingsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户私信设置仓储实现类
 * 
 * 简化设计：直接使用PO，移除Entity/Converter转换
 */
@Repository
@RequiredArgsConstructor
public class UserMessageSettingsRepository implements IUserMessageSettingsRepository {
    
    private final UserMessageSettingsMapper mapper;
    
    @Override
    public void save(UserMessageSettings settings) {
        if (settings.getId() == null) {
            mapper.insert(settings);
        } else {
            mapper.update(settings);
        }
    }
    
    @Override
    public void update(UserMessageSettings settings) {
        mapper.update(settings);
    }
    
    @Override
    public Optional<UserMessageSettings> findByUserId(Long userId) {
        UserMessageSettings po = mapper.selectByUserId(userId);
        return Optional.ofNullable(po);
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        mapper.deleteByUserId(userId);
    }
}

