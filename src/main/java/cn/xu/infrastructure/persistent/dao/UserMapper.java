package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.user.model.vo.UserFormVO;
import cn.xu.infrastructure.persistent.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface UserMapper {
    /**
     * 插入用户
     */
    void insert(User user);

    /**
     * 更新用户
     */
    void update(User user);

    /**
     * 根据ID查询用户
     */
    User selectById(Long id);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(String username);

    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(String email);

    /**
     * 统计用户名数量
     */
    int countByUsername(String username);

    /**
     * 统计邮箱数量
     */
    int countByEmail(String email);

    /**
     * 分页查询用户
     */
    List<User> selectByPage(int offset, int size);

    /**
     * 根据ID删除用户
     */
    void deleteById(Long id);

    /**
     * 批量查询用户
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
     * @param followerId
     * @param count
     */
    void updateFollowCount(@Param("followerId") Long followerId, @Param("count") int count);

    /**
     * 更新粉丝数
     *
     * @param followeeId
     * @param count
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
}
