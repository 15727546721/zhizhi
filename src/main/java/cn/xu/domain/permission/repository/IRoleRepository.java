package cn.xu.domain.permission.repository;

import cn.xu.domain.permission.model.entity.RoleEntity;

import java.util.List;

/**
 * 角色仓储接口
 */
public interface IRoleRepository {
    /**
     * 分页查询角色列表
     *
     * @param name   角色名称（模糊查询）
     * @param offset 偏移量
     * @param size   每页数量
     * @return 角色列表
     */
    List<RoleEntity> findRolePage(String name, Integer offset, Integer size);

    /**
     * 查询角色总数
     *
     * @param name 角色名称（模糊查询）
     * @return 总数
     */
    long countRole(String name);

    /**
     * 根据ID查询角色
     *
     * @param id 角色ID
     * @return 角色信息
     */
    RoleEntity findById(Long id);

    /**
     * 保存角色
     *
     * @param role 角色信息
     * @return 保存后的角色信息
     */
    RoleEntity save(RoleEntity role);

    /**
     * 删除角色
     *
     * @param ids 角色ID列表
     */
    void deleteByIds(List<Long> ids);
} 