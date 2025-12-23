package cn.xu.repository.impl;

import cn.xu.common.ResponseCode;
import cn.xu.model.entity.Role;
import cn.xu.model.entity.User;
import cn.xu.model.vo.user.UserFormVO;
import cn.xu.repository.IUserRepository;
import cn.xu.repository.mapper.RoleMapper;
import cn.xu.repository.mapper.UserMapper;
import cn.xu.repository.mapper.UserRoleMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户仓储实现
 * <p>负责用户数据的持久化操作</p>

 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

    private final UserMapper userDao;
    private final TransactionTemplate transactionTemplate;
    private final RoleMapper roleDao;
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(User userPO) {
        if (userPO.getId() == null) {
            userDao.insert(userPO);
        } else {
            userDao.update(userPO);
        }
        return userPO;
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = userDao.selectById(id);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        List<User> users = userDao.selectAll();
        return users;
    }

    @Override
    public List<User> findByPage(Integer page, Integer size) {
        int offset = (page - 1) * size;
        return userDao.selectByPage(offset, size);
    }

    @Override
    public List<User> findByConditions(String username, Integer status, Integer userType, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return userDao.selectByConditions(username, status, userType, offset, size);
    }

    @Override
    public Long countByConditions(String username, Integer status, Integer userType) {
        Long count = userDao.countByConditions(username, status, userType);
        return count != null ? count : 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        transactionTemplate.execute(status -> {
            try {
                userDao.deleteById(id);
                return null;
            } catch (Exception e) {
                log.error("删除用户失败，用户ID: " + id, e);
                status.setRollbackOnly();
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除用户失败");
            }
        });
    }

    @Override
    public List<User> findByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> users = userDao.findByIds(userIds);
        return users;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User userPO) {
        userDao.update(userPO);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        User user = userDao.selectByUsername(username);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        User user = userDao.selectByEmail(email);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> findByEmailWithPassword(String email) {
        return findByEmail(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFollowCount(Long userId, Long followCount) {
        userDao.updateUserFollowCount(userId, followCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFansCount(Long userId, Long fansCount) {
        userDao.updateUserFansCount(userId, fansCount);
    }

    @Override
    public UserFormVO findUsernameAndPasswordByUsername(String username) {
        return userDao.selectUsernameAndPasswordByUsername(username);
    }

    @Override
    public List<String> findRolesByUserId(Long userId) {
        List<Role> roles = roleDao.findRolesByUserId(userId);
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        return roleDao.findRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        transactionTemplate.execute(status -> {
            try {
                userRoleMapper.deleteByUserId(userId);
                if (roleIds != null && !roleIds.isEmpty()) {
                    userRoleMapper.saveUserRoles(userId, roleIds);
                }
                return null;
            } catch (Exception e) {
                log.error("为用户分配角色失败 userId: {}, roleIds: {}", userId, roleIds, e);
                status.setRollbackOnly();
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "为用户分配角色失败");
            }
        });
    }

    @Override
    public List<User> findUserRanking(String sortType, int offset, int limit) {
        try {
            return userDao.findUserRanking(sortType, offset, limit);
        } catch (Exception e) {
            log.error("查询用户排名失败 sortType: {}, offset: {}, limit: {}", sortType, offset, limit, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询用户排名失败");
        }
    }

    @Override
    public Long countAllUsers() {
        try {
            Long count = userDao.countAllUsers();
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("统计用户总数失败", e);
            return 0L;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void increaseFollowCount(Long userId) {
        try {
            userDao.increaseFollowCount(userId);
            log.debug("[用户仓储] 增加关注数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 增加关注数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户关注数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void decreaseFollowCount(Long userId) {
        try {
            userDao.decreaseFollowCount(userId);
            log.debug("[用户仓储] 减少关注数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 减少关注数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户关注数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void increaseFansCount(Long userId) {
        try {
            userDao.increaseFansCount(userId);
            log.debug("[用户仓储] 增加粉丝数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 增加粉丝数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户粉丝数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void decreaseFansCount(Long userId) {
        try {
            userDao.decreaseFansCount(userId);
            log.debug("[用户仓储] 减少粉丝数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 减少粉丝数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户粉丝数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void increaseLikeCount(Long userId) {
        try {
            userDao.increaseLikeCount(userId);
            log.debug("[用户仓储] 增加点赞数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 增加点赞数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户点赞数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void decreaseLikeCount(Long userId) {
        try {
            userDao.decreaseLikeCount(userId);
            log.debug("[用户仓储] 减少点赞数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 减少点赞数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户点赞数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void increasePostCount(Long userId) {
        try {
            userDao.increasePostCount(userId);
            log.debug("[用户仓储] 增加发帖数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 增加发帖数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户发帖数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void decreasePostCount(Long userId) {
        try {
            userDao.decreasePostCount(userId);
            log.debug("[用户仓储] 减少发帖数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 减少发帖数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户发帖数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void increaseCommentCount(Long userId) {
        try {
            userDao.increaseCommentCount(userId);
            log.debug("[用户仓储] 增加评论数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 增加评论数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户评论数失败");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void decreaseCommentCount(Long userId) {
        try {
            userDao.decreaseCommentCount(userId);
            log.debug("[用户仓储] 减少评论数成功- userId: {}", userId);
        } catch (Exception e) {
            log.error("[用户仓储] 减少评论数失败- userId: {}", userId, e);
            throw new BusinessException(ResponseCode.SYSTEM_ERROR.getCode(), "更新用户评论数失败");
        }
    }

    @Override
    public List<User> searchUsers(String keyword, Integer limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return userDao.searchByKeyword(keyword, limit != null ? limit : 10);
        } catch (Exception e) {
            log.error("[用户仓储] 搜索用户失败 - keyword: {}", keyword, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<User> findByIdWithPassword(Long userId) {
        // findById已包含密码字段，直接复用
        return findById(userId);
    }
}
