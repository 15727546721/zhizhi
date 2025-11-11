package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.message.model.entity.SystemConfigEntity;
import cn.xu.domain.message.repository.ISystemConfigRepository;
import cn.xu.infrastructure.persistent.converter.SystemConfigConverter;
import cn.xu.infrastructure.persistent.dao.SystemConfigMapper;
import cn.xu.infrastructure.persistent.po.SystemConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 系统配置仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class SystemConfigRepositoryImpl implements ISystemConfigRepository {
    
    private final SystemConfigMapper mapper;
    private final SystemConfigConverter converter;
    
    @Override
    public void save(SystemConfigEntity entity) {
        SystemConfig po = converter.toDataObject(entity);
        if (po.getId() == null) {
            mapper.insert(po);
            entity.setId(po.getId());
        } else {
            mapper.update(po);
        }
    }
    
    @Override
    public void update(SystemConfigEntity entity) {
        SystemConfig po = converter.toDataObject(entity);
        mapper.update(po);
    }
    
    @Override
    public Optional<SystemConfigEntity> findByKey(String configKey) {
        SystemConfig po = mapper.selectByKey(configKey);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(converter.toDomainEntity(po));
    }
    
    @Override
    public List<SystemConfigEntity> findAll() {
        List<SystemConfig> poList = mapper.selectAll();
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream()
                .map(converter::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SystemConfigEntity> findByKeyPrefix(String keyPrefix) {
        List<SystemConfig> poList = mapper.selectByKeyPrefix(keyPrefix);
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        return poList.stream()
                .map(converter::toDomainEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteByKey(String configKey) {
        mapper.deleteByKey(configKey);
    }
}

