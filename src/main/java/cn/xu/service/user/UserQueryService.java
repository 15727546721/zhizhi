package cn.xu.service.user;

import cn.xu.common.ResponseCode;
import cn.xu.model.entity.User;
import cn.xu.repository.UserRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户查询服务
 * <p>负责用户信息查询、批量查询、搜索等读操作</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    /**
     * 根据ID获取用户
     */
    public User getById(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.NOT_FOUND.getCode(), "用户不存在"));
    }

    /**
     * 根据ID获取用户（可选，不抛异常）
     */
    public Optional<User> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    /**
     * 根据邮箱获取用户
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 根据用户名获取用户
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 获取用户昵称
     */
    public String getNicknameById(Long userId) {
        return findById(userId).map(User::getNickname).orElse(null);
    }

    /**
     * 批量获取用户信息（返回Map）
     */
    public Map<Long, User> batchGetByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Set<Long> uniqueIds = new HashSet<>(userIds);
            List<User> users = userRepository.findByIds(new ArrayList<>(uniqueIds));
            return users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user, (a, b) -> a));
        } catch (Exception e) {
            log.error("批量获取用户信息失败, userIds: {}", userIds, e);
            return new HashMap<>();
        }
    }

    /**
     * 批量获取用户列表
     */
    public List<User> batchGetListByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return userRepository.findByIds(new ArrayList<>(userIds));
        } catch (Exception e) {
            log.error("批量获取用户列表失败, userIds: {}", userIds, e);
            return new ArrayList<>();
        }
    }

    /**
     * 搜索用户（支持@提及自动补全）
     */
    public List<User> searchUsers(String keyword, Integer limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return userRepository.searchUsers(keyword.trim(), limit);
        } catch (Exception e) {
            log.error("搜索用户失败: keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }

    /**
     * 分页查询用户列表
     */
    public List<User> findByPage(int pageNo, int pageSize) {
        try {
            return userRepository.findByPage(pageNo, pageSize);
        } catch (Exception e) {
            log.error("分页查询用户列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 带条件查询用户列表
     */
    public List<User> findByConditions(String username, Integer status, Integer userType, int page, int size) {
        try {
            return userRepository.findByConditions(username, status, userType, page, size);
        } catch (Exception e) {
            log.error("带条件查询用户列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 统计带条件的用户数量
     */
    public Long countByConditions(String username, Integer status, Integer userType) {
        try {
            return userRepository.countByConditions(username, status, userType);
        } catch (Exception e) {
            log.error("统计用户数量失败", e);
            return 0L;
        }
    }

    /**
     * 统计用户总数
     */
    public Long countAll() {
        try {
            return userRepository.countAllUsers();
        } catch (Exception e) {
            log.error("统计用户总数失败", e);
            return 0L;
        }
    }
}
