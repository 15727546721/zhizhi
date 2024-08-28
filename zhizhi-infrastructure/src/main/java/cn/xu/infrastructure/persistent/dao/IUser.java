package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IUser {

    void insert(@Param("userPO") UserPO userPO);

}
