package cn.xu.repository.mapper;

import cn.xu.model.entity.User;
import cn.xu.model.vo.user.UserFormVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 用户数据访问接口
 * 
 * <p>处理用户相关的数据库操作</p>

 */
@Mapper
public interface UserMapper {
    
    /**
     * 插入用户
     *
     * @param user 用户对象
     */
    void insert(User user);

    /**
     * 更新用户
     *
     * @param user 用户对象
     */
    void update(User user);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    User selectById(Long id);

    /**
     * 根据ID查询用户（含密码，用于密码验证场景）
     *
     * @param id 用户ID
     * @return 用户对象（含密码）
     */
    User selectByIdWithPassword(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    User selectByUsername(String username);

    /**
     * 根据用户名查询用户（含密码，用于密码验证场景）
     *
     * @param username 用户名
     * @return 用户对象（含密码）
     */
    User selectByUsernameWithPassword(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象
     */
    User selectByEmail(String email);

    /**
     * 统计用户名数量
     *
     * @param username 用户名
     * @return 数量
     */
    int countByUsername(String username);

    /**
     * 统计邮箱数量
     *
     * @param email 邮箱
     * @return 数量
     */
    int countByEmail(String email);

    /**
     * 分页查询用户
     *
     * @param offset 偏移量
     * @param size 查询数量
     * @return 用户列表
     */
    List<User> selectByPage(int offset, int size);

    /**
     * 带条件分页查询用户
     *
     * @param username 用户名（模糊匹配）
     * @param status 状态
     * @param userType 用户类型
     * @param offset 偏移量
     * @param size 查询数量
     * @return 用户列表
     */
    List<User> selectByConditions(@Param("username") String username, 
                                   @Param("status") Integer status, 
                                   @Param("userType") Integer userType, 
                                   @Param("offset") int offset, 
                                   @Param("size") int size);

    /**
     * 统计带条件的用户数量
     */
    Long countByConditions(@Param("username") String username, 
                           @Param("status") Integer status, 
                           @Param("userType") Integer userType);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 批量查询用户
     *
     * @param ids 用户ID集合
     * @return 用户列表
     */
    List<User> selectByIds(Set<Long> ids);

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息列表
     */
    List<User> findByIds(@Param("userIds") List<Long> userIds);

    /**
     * 更新关注数
     *
     * @param followerId 关注者ID
     * @param count 数量
     */
    void updateFollowCount(@Param("followerId") Long followerId, @Param("count") int count);

    /**
     * 更新粉丝数
     *
     * @param followeeId 被关注者ID
     * @param count 数量
     */
    void updateFansCount(@Param("followeeId") Long followeeId, @Param("count") int count);

    /**
     * 更新用户关注数
     *
     * @param id 用户ID
     * @param followCount 关注数
     */
    void updateUserFollowCount(@Param("id") Long id, @Param("followCount") Long followCount);
    
    /**
     * 更新用户粉丝数
     *
     * @param id 用户ID
     * @param fansCount 粉丝数
     */
    void updateUserFansCount(@Param("id") Long id, @Param("fansCount") Long fansCount);

    /**
     * 查询全部用户
     *
     * @return 用户列表
     */
    List<User> selectAll();
    
    /**
     * 根据用户名查找用户名和密码
     *
     * @param username 用户名
     * @return 用户表单值对象
     */
    UserFormVO selectUsernameAndPasswordByUsername(@Param("username") String username);
    
    /**
     * 查询用户排名
     *
     * <p>支持按粉丝数、获赞数、帖子数和综合排名</p>
     *
     * @param sortType 排序类型：fans(粉丝数)、likes(获赞数)、posts(帖子数)、comprehensive(综合)
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 用户列表（包含帖子数统计）
     */
    List<User> findUserRanking(@Param("sortType") String sortType, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计用户总数（用于排名统计）
     *
     * @return 用户总数
     */
    Long countAllUsers();
    
    // ==================== 原子更新方法（避免并发问题） ====================
    
    /**
     * 原子增加关注数（+1）
     * 
     * <p>使用SQL的自增操作，避免并发问题</p>
     * 
     * @param userId 用户ID
     */
    void increaseFollowCount(@Param("userId") Long userId);
    
    /**
     * 原子减少关注数（-1）
     * 
     * @param userId 用户ID
     */
    void decreaseFollowCount(@Param("userId") Long userId);
    
    /**
     * 原子增加粉丝数（+1）
     * 
     * @param userId 用户ID
     */
    void increaseFansCount(@Param("userId") Long userId);
    
    /**
     * 原子减少粉丝数（-1）
     * 
     * @param userId 用户ID
     */
    void decreaseFansCount(@Param("userId") Long userId);
    
    /**
     * 原子增加获赞数（+1）
     * 
     * @param userId 用户ID
     */
    void increaseLikeCount(@Param("userId") Long userId);
    
    /**
     * 原子减少获赞数（-1）
     * 
     * @param userId 用户ID
     */
    void decreaseLikeCount(@Param("userId") Long userId);
    
    /**
     * 原子增加发帖数（+1）
     * 
     * @param userId 用户ID
     */
    void increasePostCount(@Param("userId") Long userId);
    
    /**
     * 原子减少发帖数（-1）
     * 
     * @param userId 用户ID
     */
    void decreasePostCount(@Param("userId") Long userId);
    
    /**
     * 原子增加评论数（+1）
     * 
     * @param userId 用户ID
     */
    void increaseCommentCount(@Param("userId") Long userId);
    
    /**
     * 原子减少评论数（-1）
     * 
     * @param userId 用户ID
     */
    void decreaseCommentCount(@Param("userId") Long userId);
    
    // ==================== 定时校验相关方法 ====================
    
    /**
     * 批量更新用户统计字段
     * 
     * <p>用于定时校验任务，确保统计数据准确</p>
     * 
     * @param userId 用户ID
     * @param followCount 关注数
     * @param fansCount 粉丝数
     * @param likeCount 获赞数
     * @param postCount 发帖数
     * @param commentCount 评论数
     */
    void updateUserCounts(
        @Param("userId") Long userId,
        @Param("followCount") Long followCount,
        @Param("fansCount") Long fansCount,
        @Param("likeCount") Long likeCount,
        @Param("postCount") Long postCount,
        @Param("commentCount") Long commentCount
    );
    
    /**
     * 搜索用户（支持username和nickname模糊查询）
     * 
     * <p>用于@自动补全功能</p>
     * 
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 用户列表
     */
    List<User> searchByUsernameOrNickname(@Param("keyword") String keyword, @Param("limit") Integer limit);
    
    /**
     * 统计所有用户数
     *
     * @return 用户总数
     */
    Long countAll();
    
    /**
     * 按状态统计用户数
     *
     * @param status 用户状态
     * @return 用户数
     */
    Long countByStatus(@Param("status") Integer status);
    
    /**
     * 根据关键词搜索用户
     *
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 用户列表
     */
    List<User> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);
    
    /**
     * 查询所有有效用户的ID
     *
     * <p>用于群发系统通知</p>
     *
     * @return 用户ID列表
     */
    List<Long> selectAllUserIds();
}
