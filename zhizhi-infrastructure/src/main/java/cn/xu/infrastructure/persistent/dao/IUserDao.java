package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.UserPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IUserDao {

    void insert(@Param("userPO") UserPO userPO);

}
