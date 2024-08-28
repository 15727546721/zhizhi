package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.UserOrderPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IUserOrderDao {

    void insert(UserOrderPO userOrderPO);

    void updateOrderStatusByUserId(String userId);

    List<UserOrderPO> selectByUserId(String userId);

}
