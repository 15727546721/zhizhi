package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.model.valobj.Username;
import cn.xu.domain.user.model.vo.UserFormVO;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 * 遵循DDD原则，只处理用户实体的基本CRUD操作
 */
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
    Optional<UserEntity> findByUsername(Username username);

    /**
     * 根据邮箱查找用户
     */
    Optional<UserEntity> findByEmail(Email email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(Username username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(Email email);

    /**
     * 分页查询所有用户
     */
    List<UserEntity> findByPage(Integer page, Integer size);

    /**
     * 查询所有用户
     */
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
     * 更新用户信息
     *
     * @param userEntity 用户实体
     */
    void update(UserEntity userEntity);
    
    /**
     * 根据用户名查找用户名和密码
     *
     * @param username 用户名
     * @return 用户表单值对象
     */
    UserFormVO findUsernameAndPasswordByUsername(String username);
    
    /**
     * 根据用户ID查找用户信息实体
     *
     * @param userId 用户ID
     * @return 用户信息实体
     */
    UserInfoEntity findUserInfoById(Long userId);
    
    /**
     * 根据用户ID查找角色列表
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> findRolesByUserId(Long userId);
    
    /**
     * 更新用户的关注数
     * 
     * @param userId 用户ID
     * @param followCount 关注数
     */
    void updateFollowCount(Long userId, Long followCount);
    
    /**
     * 更新用户的粉丝数
     * 
     * @param userId 用户ID
     * @param fansCount 粉丝数
     */
    void updateFansCount(Long userId, Long fansCount);
}