package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.aggregate.UserAggregate;
import cn.xu.domain.user.model.valobj.Email;

import java.util.List;
import java.util.Optional;

/**
 * 用户聚合根仓储接口
 * 负责用户聚合根的持久化操作
 */
public interface IUserAggregateRepository {
    /**
     * 保存用户聚合根
     *
     * @param aggregate 用户聚合根
     * @return 用户ID
     */
    Long save(UserAggregate aggregate);

    /**
     * 更新用户聚合根
     *
     * @param aggregate 用户聚合根
     */
    void update(UserAggregate aggregate);

    /**
     * 根据ID查找用户聚合根
     *
     * @param id 用户ID
     * @return 用户聚合根
     */
    Optional<UserAggregate> findById(Long id);

    /**
     * 根据用户名查找用户聚合根
     *
     * @param username 用户名
     * @return 用户聚合根
     */
    Optional<UserAggregate> findByUsername(String username);

    /**
     * 根据邮箱查找用户聚合根
     *
     * @param email 邮箱
     * @return 用户聚合根
     */
    Optional<UserAggregate> findByEmail(Email email);

    /**
     * 分页查询用户聚合根
     *
     * @param pageNo   页码
     * @param pageSize 页面大小
     * @return 用户聚合根列表
     */
    List<UserAggregate> findByPage(Integer pageNo, Integer pageSize);

    /**
     * 批量查询用户聚合根
     *
     * @param userIds 用户ID列表
     * @return 用户聚合根列表
     */
    List<UserAggregate> findByIds(List<Long> userIds);

    /**
     * 根据ID删除用户聚合根
     *
     * @param id 用户ID
     */
    void deleteById(Long id);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(Email email);
}