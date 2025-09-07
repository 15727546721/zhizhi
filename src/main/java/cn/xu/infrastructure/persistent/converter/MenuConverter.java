package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.infrastructure.persistent.po.Menu;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单权限领域实体与持久化对象转换器
 * 符合DDD架构的防腐层模式
 * 
 * @author xu
 */
@Component
public class MenuConverter {

    /**
     * 将领域实体转换为持久化对象
     *
     * @param entity 菜单领域实体
     * @return 菜单持久化对象
     */
    public Menu toDataObject(MenuEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return Menu.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .path(entity.getPath())
                .component(entity.getComponent())
                .icon(entity.getIcon())
                .sort(entity.getSort())
                .parentId(entity.getParentId())
                .type(entity.getType())
                .hidden(entity.getHidden())
                .perm(entity.getPerm())
                .redirect(entity.getRedirect())
                .name(entity.getName())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param po 菜单持久化对象
     * @return 菜单领域实体
     */
    public MenuEntity toDomainEntity(Menu po) {
        if (po == null) {
            return null;
        }
        
        return MenuEntity.builder()
                .id(po.getId())
                .title(po.getTitle())
                .path(po.getPath())
                .component(po.getComponent())
                .icon(po.getIcon())
                .sort(po.getSort())
                .parentId(po.getParentId())
                .type(po.getType())
                .hidden(po.getHidden())
                .perm(po.getPerm())
                .redirect(po.getRedirect())
                .name(po.getName())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    /**
     * 批量转换持久化对象为领域实体
     *
     * @param poList 持久化对象列表
     * @return 领域实体列表
     */
    public List<MenuEntity> toDomainEntities(List<Menu> poList) {
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }
        
        return poList.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换领域实体为持久化对象
     *
     * @param entities 领域实体列表
     * @return 持久化对象列表
     */
    public List<Menu> toDataObjects(List<MenuEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        
        return entities.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }
}