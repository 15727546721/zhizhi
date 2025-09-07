package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.user.model.entity.RoleEntity;
import cn.xu.infrastructure.persistent.po.Role;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色领域实体与持久化对象转换器
 * 专用于UserAggregate中的RoleEntity转换
 * 符合DDD架构的防腐层模式
 * 
 * @author xu
 */
@Component
public class UserRoleConverter {

    /**
     * 将用户角色领域实体转换为持久化对象
     *
     * @param entity 用户角色领域实体
     * @return 角色持久化对象
     */
    public Role toDataObject(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Role.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .remark(entity.getDesc())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 将持久化对象转换为用户角色领域实体
     *
     * @param po 角色持久化对象
     * @return 用户角色领域实体
     */
    public RoleEntity toDomainEntity(Role po) {
        if (po == null) {
            return null;
        }
        
        return RoleEntity.builder()
                .id(po.getId())
                .name(po.getName())
                .code(po.getCode())
                .desc(po.getRemark())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    /**
     * 批量转换持久化对象为用户角色领域实体
     *
     * @param poList 持久化对象列表
     * @return 用户角色领域实体列表
     */
    public List<RoleEntity> toDomainEntities(List<Role> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        
        return poList.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换用户角色领域实体为持久化对象
     *
     * @param entities 用户角色领域实体列表
     * @return 持久化对象列表
     */
    public List<Role> toDataObjects(List<RoleEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}