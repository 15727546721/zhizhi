package cn.xu.repository.impl;

import cn.xu.model.entity.Role;
import cn.xu.repository.IRoleRepository;
import cn.xu.repository.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 角色仓储实现类
 * <p>负责角色数据的持久化操作</p>

 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepository implements IRoleRepository {

    private final RoleMapper roleDao;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Role> findRolePage(String name, Integer offset, Integer size) {
        return roleDao.selectRolePage(name, offset, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long countRole(String name) {
        return roleDao.countRole(name);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role findById(Long id) {
        return roleDao.selectRoleById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role save(Role role) {
        if (role.getId() == null) {
            roleDao.insertRole(role);
        } else {
            roleDao.updateRole(role);
        }
        return role;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> ids) {
        transactionTemplate.execute(status -> {
            try {
                roleDao.deleteRoleByIds(ids);
                roleDao.deleteRoleMenuByRoleIds(ids);
                return null;
            } catch (Exception e) {
                log.error("删除角色失败 - 角色ID: {}", ids, e);
                status.setRollbackOnly();
                throw e;
            }
        });
    }
    
    /**
     * 根据角色编码查询角色
     * @param code 角色编码
     * @return 角色实体
     */
    @Override
    public Role findByCode(String code) {
        return roleDao.selectRoleByCode(code);
    }
}
