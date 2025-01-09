package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.model.valueobject.Email;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IUserRepository {
    /**
     * 保存用户信息
     */
    UserEntity save(UserEntity user);

    /**
     * 根据用户ID查找用户
     */
    Optional<UserEntity> findById(Long id);

    /**
     * 根据用户名查找用户
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<UserEntity> findByEmail(Email email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(Email email);

    /**
     * 分页查询所有用户
     */
    List<UserEntity> findAll(Integer page, Integer size);

    /**
     * 根据ID删除用户
     */
    void deleteById(Long id);

    /**
     * 批量查询用户信息
     */
    List<UserEntity> findByIds(Set<Long> userIds);

    /**
     * 根据用户名查找用户登录信息
     */
    LoginFormVO findUserByUsername(String username);

    /**
     * 根据用户ID查找用户详细信息
     */
    UserInfoEntity findUserInfoById(Long userId);

    /**
     * 根据用户ID查询角色列表
     */
    List<String> findRolesByUserId(Long userId);
}