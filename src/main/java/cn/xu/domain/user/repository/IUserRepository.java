package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserRegisterEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.model.vo.LoginFormVO;
import cn.xu.domain.user.model.vo.UserFormVO;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    /**
     * 保存用户信息
     */
    UserEntity save(UserEntity user);

    /**
     * 根据用户ID查找用户
     */
    UserEntity findById(Long id);

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
    List<UserEntity> findByPage(Integer page, Integer size);

    List<UserEntity> findAll();

    /**
     * 根据ID删除用户
     */
    void deleteById(Long id);

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息列表
     */
    List<UserEntity> findByIds(List<Long> userIds);

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

    /**
     * 根据用户ID查询用户名
     *
     * @param userId
     * @return
     */
    String getNicknameById(Long userId);

    /**
     * 更新用户信息
     *
     * @param userEntity
     */
    void update(UserEntity userEntity);

    /**
     * 根据用户名查询用户信息
     *
     * @param username
     * @return
     */
    UserFormVO findUsernameAndPasswordByUsername(String username);

    /**
     * 注册用户
     *
     * @param user
     * @return
     */
    long register(UserRegisterEntity user);
}