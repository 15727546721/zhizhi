package cn.xu.repository;

import cn.xu.model.entity.Role;

import java.util.List;

/**
 * 角色仓储接口
 */
public interface IRoleRepository {
    /**
     * 分页查询角色列表
     */
    List<Role> findRolePage(String name, Integer offset, Integer size);

    /**
     * 查询角色总数
     */
    long countRole(String name);

    /**
     * 根据ID查询角色
     */
    Role findById(Long id);

    /**
     * 保存角色
     */
    Role save(Role role);

    /**
     * 根据ID删除角色
     */
    void deleteByIds(List<Long> ids);
    
    /**
     * 根据角色编码查询角色
     * @param code 角色编码
     * @return 角色实体
     */
    Role findByCode(String code);
}