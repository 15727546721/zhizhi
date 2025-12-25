package cn.xu.repository;

import cn.xu.model.entity.User;
import cn.xu.model.vo.user.UserFormVO;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface UserRepository {
    /**
     * 保存用户
     */
    User save(User user);

    /**
     * 根据用户ID查找用户
     */
    Optional<User> findById(Long id);

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据邮箱查找用户（包含密码）
     */
    Optional<User> findByEmailWithPassword(String email);

    /**
     * 分页查询所有用户
     */
    List<User> findByPage(Integer page, Integer size);

    /**
     * 按条件分页查询用户
     */
    List<User> findByConditions(String username, Integer status, Integer userType, Integer page, Integer size);

    /**
     * 统计按条件的用户数量
     */
    Long countByConditions(String username, Integer status, Integer userType);

    /**
     * 查询所有用户
     */
    List<User> findAll();

    /**
     * 根据ID删除用户
     */
    void deleteById(Long id);

    /**
     * 批量查询用户信息
     */
    List<User> findByIds(List<Long> userIds);

    /**
     * 更新用户信息
     */
    void update(User userEntity);

    /**
     * 根据用户名查找用户名和密码
     */
    UserFormVO findUsernameAndPasswordByUsername(String username);

    /**
     * 根据用户ID查找角色名称列表
     */
    List<String> findRolesByUserId(Long userId);

    /**
     * 根据用户ID查找角色ID列表
     */
    List<Long> findRoleIdsByUserId(Long userId);

    /**
     * 为用户分配角色
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 更新用户的关注数
     */
    void updateFollowCount(Long userId, Long followCount);

    /**
     * 更新用户的粉丝数
     */
    void updateFansCount(Long userId, Long fansCount);

    // ==================== 原子更新方法 ====================

    void increaseFollowCount(Long userId);
    void decreaseFollowCount(Long userId);
    void increaseFansCount(Long userId);
    void decreaseFansCount(Long userId);
    void increaseLikeCount(Long userId);
    void decreaseLikeCount(Long userId);
    void increasePostCount(Long userId);
    void decreasePostCount(Long userId);
    void increaseCommentCount(Long userId);
    void decreaseCommentCount(Long userId);

    /**
     * 根据用户名查找用户实体
     */
    default User findUserEntityByUsername(String username) {
        return findByUsername(username).orElse(null);
    }

    /**
     * 查询用户排名
     */
    List<User> findUserRanking(String sortType, int offset, int limit);

    /**
     * 统计用户总数
     */
    Long countAllUsers();

    /**
     * 搜索用户
     */
    List<User> searchUsers(String keyword, Integer limit);

    /**
     * 根据用户ID查找用户（包含密码字段）
     */
    Optional<User> findByIdWithPassword(Long userId);
}
