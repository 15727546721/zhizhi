package cn.xu.infrastructure.persistent.dao;

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
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    User selectByEmail(@Param("email") String email);

    /**
     * 统计用户名数量
     */
    int countByUsername(@Param("username") String username);

    /**
     * 统计邮箱数量
     */
    int countByEmail(@Param("email") String email);

    /**
     * 分页查询用户
     */
    List<User> selectByPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 根据ID删除用户
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量查询用户
     */
    List<User> selectByIds(@Param("ids") Set<Long> ids);
}
