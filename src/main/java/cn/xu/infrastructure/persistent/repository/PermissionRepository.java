package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.infrastructure.persistent.dao.IMenuDao;
import cn.xu.infrastructure.persistent.dao.IRoleDao;
import cn.xu.infrastructure.persistent.po.Menu;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

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
    public List<Menu> selectMenuList() {
        List<Menu> menuList = menuDao.selectMenuList();
        return menuList;
    }
}
