package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.infrastructure.persistent.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface IUserDao {
    /**
     * 插入用户
     */
    void insert(UserEntity user);

    /**
     * 更新用户
     */
    void update(UserEntity user);

    /**
     * 根据ID查询用户
     */
    UserEntity selectById(Long id);

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
     * 根据用户ID查询用户信息和角色
     */
    UserInfoEntity getUserInfoWithRoles(@Param("userId") Long userId);

    /**
     * 根据用户名查询用户信息和角色
     */
    UserInfoEntity getUserInfoWithRolesByUsername(@Param("username") String username);

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息列表
     */
    List<User> findByIds(@Param("userIds") Set<Long> userIds);

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
}
