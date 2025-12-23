package cn.xu.repository;

import cn.xu.model.entity.User;
import cn.xu.model.vo.user.UserFormVO;

import java.util.List;
import java.util.Optional;

/**
 * 用户仓储接口
 */
public interface IUserRepository {
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
     *
     * @param username 用户名（模糊匹配）
     * @param status 状态
     * @param userType 用户类型
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
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
     *
     * @param userIds 用户ID集合
     * @return 用户信息列表
     */
    List<User> findByIds(List<Long> userIds);

    /**
     * 更新用户信息
     *
     * @param userEntity 用户实体
     */
    void update(User userEntity);

    /**
     * 根据用户名查找用户名和密码
     *
     * @param username 用户名
     * @return 用户表单值对象
     */
    UserFormVO findUsernameAndPasswordByUsername(String username);

    /**
     * 根据用户ID查找角色名称列表
     *
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> findRolesByUserId(Long userId);

    /**
     * 根据用户ID查找角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> findRoleIdsByUserId(Long userId);

    /**
     * 为用户分配角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 更新用户的关注数（设置绝对值）
     *
     * @param userId     用户ID
     * @param followCount 关注数
     */
    void updateFollowCount(Long userId, Long followCount);

    /**
     * 更新用户的粉丝数（设置绝对值）
     *
     * @param userId   用户ID
     * @param fansCount 粉丝数
     */
    void updateFansCount(Long userId, Long fansCount);

    // ==================== 原子更新方法（解决并发问题） ====================

    /**
     * 原子增加关注数（+1）
     * 使用 SQL 原子操作，避免并发问题
     *
     * @param userId 用户ID
     */
    void increaseFollowCount(Long userId);

    /**
     * 原子减少关注数（-1）
     *
     * @param userId 用户ID
     */
    void decreaseFollowCount(Long userId);

    /**
     * 原子增加粉丝数（+1）
     *
     * @param userId 用户ID
     */
    void increaseFansCount(Long userId);

    /**
     * 原子减少粉丝数（-1）
     *
     * @param userId 用户ID
     */
    void decreaseFansCount(Long userId);

    /**
     * 原子增加点赞数（+1）
     *
     * @param userId 用户ID
     */
    void increaseLikeCount(Long userId);

    /**
     * 原子减少点赞数（-1）
     *
     * @param userId 用户ID
     */
    void decreaseLikeCount(Long userId);

    /**
     * 原子增加发帖数（+1）
     *
     * @param userId 用户ID
     */
    void increasePostCount(Long userId);

    /**
     * 原子减少发帖数（-1）
     *
     * @param userId 用户ID
     */
    void decreasePostCount(Long userId);

    /**
     * 原子增加评论数（+1）
     *
     * @param userId 用户ID
     */
    void increaseCommentCount(Long userId);

    /**
     * 原子减少评论数（-1）
     *
     * @param userId 用户ID
     */
    void decreaseCommentCount(Long userId);

    /**
     * 根据用户名查找用户实体
     *
     * @param username 用户名
     * @return 用户实体
     */
    default User findUserEntityByUsername(String username) {
        return findByUsername(username).orElse(null);
    }

    /**
     * 查询用户排名
     *
     * @param sortType 排序类型：fans(粉丝数)、likes(点赞数)、posts(帖子数)、comprehensive(综合)
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 用户列表（包含帖子数统计）
     */
    List<User> findUserRanking(String sortType, int offset, int limit);

    /**
     * 统计用户总数（用于排名缓存）
     *
     * @return 用户总数
     */
    Long countAllUsers();

    /**
     * 搜索用户（支持模糊匹配和自动补全）
     *
     * @param keyword 关键字（用户名或昵称）
     * @param limit 限制数量
     * @return 用户列表
     */
    List<User> searchUsers(String keyword, Integer limit);

    /**
     * 根据用户ID查找用户（包含密码字段）
     * 用于需要验证密码的场景（如登录账号）
     *
     * @param userId 用户ID
     * @return 用户信息（包含密码）
     */
    Optional<User> findByIdWithPassword(Long userId);
}
