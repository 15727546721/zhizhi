package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会员Mapper接口
 */
@Mapper
public interface MemberMapper {
    
    /**
     * 插入会员记录
     */
    void insert(Member member);
    
    /**
     * 更新会员记录
     */
    void update(Member member);
    
    /**
     * 根据用户ID查询会员记录
     */
    Member findByUserId(Long userId);
    
    /**
     * 查询积分排行榜
     */
    List<Member> findPointsRanking(@Param("limit") int limit);
    
    /**
     * 查询等级排行榜
     */
    List<Member> findLevelRanking(@Param("limit") int limit);
}