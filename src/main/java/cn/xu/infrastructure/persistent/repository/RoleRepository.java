package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.repository.IRoleRepository;
import cn.xu.infrastructure.persistent.converter.RoleConverter;
import cn.xu.infrastructure.persistent.dao.RoleMapper;
import cn.xu.infrastructure.persistent.po.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * 角色仓储实现类
 * 通过RoleConverter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RoleRepository implements IRoleRepository {

    private final RoleMapper roleDao;
    private final TransactionTemplate transactionTemplate;
    private final RoleConverter roleConverter;

    @Override
    public List<RoleEntity> findRolePage(String name, Integer offset, Integer size) {
        List<Role> roles = roleDao.selectRolePage(name, offset, size);
        return roleConverter.toDomainEntities(roles);
    }

    @Override
    public long countRole(String name) {
        return roleDao.countRole(name);
    }

    @Override
    public RoleEntity findById(Long id) {
        Role role = roleDao.selectRoleById(id);
        return roleConverter.toDomainEntity(role);
    }

    @Override
    public RoleEntity save(RoleEntity role) {
        Role rolePO = roleConverter.toDataObject(role);
        if (role.getId() == null) {
            roleDao.insertRole(rolePO);
            role.setId(rolePO.getId());
        } else {
            roleDao.updateRole(rolePO);
        }
        return role;
    }
    @Override
    public void deleteByIds(List<Long> ids) {
        transactionTemplate.execute(status -> {
            try {
                roleDao.deleteRoleByIds(ids);
                roleDao.deleteRoleMenuByRoleIds(ids);
                return null;
            } catch (Exception e) {
                log.error("删除角色失败, 角色ID: {}", ids, e);
                status.setRollbackOnly();
                throw e;
            }
        });
    }
} 