package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.infrastructure.persistent.dao.IRoleDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class PermissionRepository implements IPermissionRepository {

    @Resource
    private IRoleDao roleDao;

    @Override
    public List<String> findRolesByUserid(Long userid) {
        List<String> roles = roleDao.selectRolesByUserid(userid);
        return roles;
    }
}
