package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserPasswordEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.infrastructure.persistent.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface IUserDao {

    LoginFormVO selectUserByUserName(@Param("username") String username);

    User selectUserById(@Param("userId") Long userId);

    UserInfoEntity selectUserInfoById(@Param("userId") Long userId);

    List<User> selectUserByPage(@Param("page") int page, @Param("size") int size);

    List<User> selectAdminByPage(@Param("page") int page, @Param("size") int size);

    Long insertUser(@Param("user") User user);

    int updateUser(@Param("user") User user);

    int deleteUser(@Param("userId") Long userId);

    void updatePassword(UserPasswordEntity userPasswordEntity);

    UserInfoEntity selectUserInfoByUserId(@Param("userId") Long id);

    void updateUserInfo(UserInfoEntity userInfoEntity);

    void updateAvatar(@Param("userId") Long id, @Param("avatar") String avatar);

    void register(User user);

    /**
     * 根据邮箱和密码查询用户
     *
     * @param email
     * @param password
     * @return 用户ID
     */
    UserEntity findUserLoginByEmailAndPassword(@Param("email") String email, @Param("password") String password);

    /**
     * 根据用户ID集合查询用户列表
     *
     * @param userIds
     * @return 用户列表
     */
    List<UserEntity> findUserByIds(@Param("userIds") Set<Long> userIds);
}
