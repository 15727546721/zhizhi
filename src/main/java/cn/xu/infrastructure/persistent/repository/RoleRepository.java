package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.permission.model.entity.RoleEntity;
import cn.xu.domain.permission.repository.IRoleRepository;
import cn.xu.infrastructure.persistent.dao.IRoleDao;
import cn.xu.infrastructure.persistent.po.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class RoleRepository implements IRoleRepository {

    @Resource
    private IRoleDao roleDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public List<RoleEntity> findRolePage(String name, Integer offset, Integer size) {
        return roleDao.selectRolePage(name, offset, size)
                .stream()
                .map(this::convertToRoleEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countRole(String name) {
        return roleDao.countRole(name);
    }

    @Override
    public RoleEntity findById(Long id) {
        return convertToRoleEntity(roleDao.selectRoleById(id));
    }

    @Override
    public RoleEntity save(RoleEntity role) {
        Role rolePO = convertToRolePO(role);
        if (role.getId() == null) {
            roleDao.insertRole(rolePO);
        } else {
            roleDao.updateRole(rolePO);
        }
        return convertToRoleEntity(rolePO);
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

    private RoleEntity convertToRoleEntity(Role role) {
        if (role == null) {
            return null;
        }
        return RoleEntity.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .build();
    }

    private Role convertToRolePO(RoleEntity role) {
        return Role.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .build();
    }
} 