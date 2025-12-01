package cn.xu.repository.impl;

import cn.xu.model.entity.SystemConfig;
import cn.xu.repository.ISystemConfigRepository;
import cn.xu.repository.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 系统配置仓储实现类
 * 
 * 简化设计：直接使用PO，移除Entity/Converter转换
 */
@Repository
@RequiredArgsConstructor
public class SystemConfigRepository implements ISystemConfigRepository {
    
    private final SystemConfigMapper mapper;
    
    @Override
    public void save(SystemConfig config) {
        if (config.getId() == null) {
            mapper.insert(config);
        } else {
            mapper.update(config);
        }
    }
    
    @Override
    public void update(SystemConfig config) {
        mapper.update(config);
    }
    
    @Override
    public Optional<SystemConfig> findByKey(String configKey) {
        SystemConfig po = mapper.selectByKey(configKey);
        return Optional.ofNullable(po);
    }
    
    @Override
    public List<SystemConfig> findAll() {
        List<SystemConfig> poList = mapper.selectAll();
        return poList != null ? poList : Collections.emptyList();
    }
    
    @Override
    public List<SystemConfig> findByKeyPrefix(String keyPrefix) {
        List<SystemConfig> poList = mapper.selectByKeyPrefix(keyPrefix);
        return poList != null ? poList : Collections.emptyList();
    }
    
    @Override
    public void deleteByKey(String configKey) {
        mapper.deleteByKey(configKey);
    }
}

