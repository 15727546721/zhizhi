package cn.xu.domain.permission.repository;

import cn.xu.domain.permission.model.entity.RoleEntity;

import java.util.List;

/**
 * 角色仓储接口
 */
public interface IRoleRepository {
    /**
     * 分页查询角色列表
     */
    List<RoleEntity> findRolePage(String name, Integer offset, Integer size);

    /**
     * 查询角色总数
     */
    long countRole(String name);

    /**
     * 根据ID查询角色
     */
    RoleEntity findById(Long id);

    /**
     * 保存角色
     */
    RoleEntity save(RoleEntity role);

    /**
     * 根据ID删除角色
     */
    void deleteByIds(List<Long> ids);
    
    /**
     * 根据角色编码查询角色
     * @param code 角色编码
     * @return 角色实体
     */
    RoleEntity findByCode(String code);
}