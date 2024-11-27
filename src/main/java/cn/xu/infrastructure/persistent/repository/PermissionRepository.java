package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.permission.model.entity.MenuEntity;
import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.infrastructure.persistent.dao.IMenuDao;
import cn.xu.infrastructure.persistent.dao.IRoleDao;
import cn.xu.infrastructure.persistent.po.Menu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PermissionRepository implements IPermissionRepository {

    @Resource
    private IRoleDao roleDao;
    @Resource
    private IMenuDao menuDao;

    @Override
    public List<String> findRolesByUserid(Long userid) {
        List<String> roles = roleDao.selectRolesByUserid(userid);
        return roles;
    }

    @Override
    public List<MenuEntity> selectMenuList() {
        List<Menu> menuList = menuDao.selectMenuList();
        List<MenuEntity> menuEntityList = menuList.stream()
                .map(this::convert)
                .collect(Collectors.toList());
        return menuEntityList;
    }

    @Override
    public MenuEntity selectMenuById(Long id) {
        Menu menu = menuDao.selectMenuById(id);
        return convert(menu);
    }

    private MenuEntity convert(Menu menu) {
        if (menu == null) {
            return null;
        }
        return MenuEntity.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .path(menu.getPath())
                .component(menu.getComponent())
                .title(menu.getTitle())
                .sort(menu.getSort())
                .icon(menu.getIcon())
                .type(menu.getType())
                .createdTime(menu.getCreatedTime())
                .updateTime(menu.getUpdateTime())
                .redirect(menu.getRedirect())
                .name(menu.getName())
                .hidden(menu.getHidden())
                .perm(menu.getPerm())
                .children(new ArrayList<>()) // 初始化子菜单列表
                .build();
    }
}
