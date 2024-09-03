package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.infrastructure.persistent.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IUserDao {

    LoginFormVO selectUserByUserName(@Param("username") String username);
    UserEntity selectUserById(@Param("userId") Long userId);

    UserInfoEntity selectUserInfoById(@Param("userId") Long userId);

    List<UserEntity> selectUserByPage(@Param("page") int page, @Param("size") int size);

    List<UserEntity> selectAdminByPage(@Param("page") int page, @Param("size") int size);

    int insertUser(@Param("user") User user);

    int updateUser(@Param("user") User user);

    int deleteUser(@Param("userId") Long userId);
}
