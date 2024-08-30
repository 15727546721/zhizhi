package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.infrastructure.persistent.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IUserDao {

    void insert(@Param("userPO") UserPO userPO);
    LoginFormVO selectUserByUserName(@Param("username") String username);
    UserEntity selectUserById(@Param("userId") Long userId);

    UserInfoEntity selectUserInfoById(@Param("userId") Long userId);
}
