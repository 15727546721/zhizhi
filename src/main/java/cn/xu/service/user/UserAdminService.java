package cn.xu.service.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.constants.RoleConstants;
import cn.xu.model.dto.user.SysUserRequest;
import cn.xu.model.entity.User;
import cn.xu.model.enums.UserType;
import cn.xu.repository.UserRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理服务（管理员操作）
 * <p>负责管理员对用户的增删改查操作</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final UserQueryService queryService;

    /**
     * 添加用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUserRequest request) {
        if (request == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户信息不能为空");
        }

        try {
            // 检查邮箱是否已存在
            if (StringUtils.isNotBlank(request.getEmail())) {
                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                    throw new BusinessException(ResponseCode.USER_ALREADY_EXISTS.getCode(), "该邮箱已被使用");
                }
            }

            User newUser = User.createNewUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getUsername(),
                    request.getNickname()
            );

            // 设置附加属性
            if (StringUtils.isNotBlank(request.getPhone())) {
                newUser.setPhone(request.getPhone());
            }
            if (StringUtils.isNotBlank(request.getAvatar())) {
                newUser.setAvatar(request.getAvatar());
            }
            if (request.getGender() != null) {
                newUser.setGender(request.getGender());
            }
            if (StringUtils.isNotBlank(request.getDescription())) {
                newUser.setDescription(request.getDescription());
            }
            if (request.getUserType() != null) {
                newUser.setUserType(request.getUserType());
            }
            if (request.getStatus() != null) {
                newUser.setStatus(request.getStatus());
            }

            userRepository.save(newUser);
            log.info("管理员添加用户成功, email: {}", request.getEmail());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员添加用户失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "添加用户失败，请稍后重试");
        }
    }

    /**
     * 更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户信息不能为空");
        }

        try {
            User user = queryService.getById(request.getId());

            if (StringUtils.isNotBlank(request.getUsername())) {
                user.setUsername(request.getUsername());
            }
            if (StringUtils.isNotBlank(request.getNickname())) {
                user.setNickname(request.getNickname());
            }
            if (StringUtils.isNotBlank(request.getEmail())) {
                // 检查邮箱是否已被其他用户使用
                userRepository.findByEmail(request.getEmail())
                        .filter(u -> !u.getId().equals(request.getId()))
                        .ifPresent(u -> {
                            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "该邮箱已被其他用户使用");
                        });
                user.setEmail(request.getEmail());
            }
            if (StringUtils.isNotBlank(request.getPhone())) {
                user.setPhone(request.getPhone());
            }
            if (StringUtils.isNotBlank(request.getAvatar())) {
                user.setAvatar(request.getAvatar());
            }
            if (request.getGender() != null) {
                user.setGender(request.getGender());
            }
            if (StringUtils.isNotBlank(request.getDescription())) {
                user.setDescription(request.getDescription());
            }
            if (request.getUserType() != null) {
                user.setUserType(request.getUserType());
            }
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }

            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            log.info("管理员更新用户成功, userId: {}", request.getId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员更新用户失败, userId: {}", request.getId(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新用户失败，请稍后重试");
        }
    }

    /**
     * 批量删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        try {
            userRepository.batchUpdateStatus(userIds, User.STATUS_DELETED);

            for (Long userId : userIds) {
                try {
                    StpUtil.kickout(userId);
                } catch (Exception e) {
                    log.warn("踢出用户登录失败, userId: {}", userId);
                }
            }
            log.info("批量删除用户成功, userIds: {}", userIds);
        } catch (Exception e) {
            log.error("批量删除用户失败, userIds: {}", userIds, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量删除用户失败");
        }
    }

    /**
     * 初始化/重置管理员账号
     */
    @Transactional(rollbackFor = Exception.class)
    public User initAdminUser() {
        try {
            User admin = userRepository.findByUsername(RoleConstants.DEFAULT_ADMIN_USERNAME).orElse(null);

            if (admin == null) {
                admin = new User();
                admin.setUsername(RoleConstants.DEFAULT_ADMIN_USERNAME);
                admin.setNickname(RoleConstants.DEFAULT_ADMIN_NICKNAME);
                admin.setEmail(RoleConstants.DEFAULT_ADMIN_EMAIL);
                admin.setUserType(UserType.ADMIN.getCode());
                admin.setStatus(User.STATUS_NORMAL);
                admin.setPassword(RoleConstants.DEFAULT_ADMIN_PASSWORD);
                admin.encryptPassword();
                admin.setCreateTime(LocalDateTime.now());
                admin.setUpdateTime(LocalDateTime.now());
                userRepository.save(admin);
                log.info("创建管理员账号成功: {}", RoleConstants.DEFAULT_ADMIN_USERNAME);
            } else {
                admin.setPassword(RoleConstants.DEFAULT_ADMIN_PASSWORD);
                admin.encryptPassword();
                admin.setUserType(UserType.ADMIN.getCode());
                admin.setStatus(User.STATUS_NORMAL);
                admin.setUpdateTime(LocalDateTime.now());
                userRepository.save(admin);
                log.info("重置管理员账号密码成功: {}", RoleConstants.DEFAULT_ADMIN_USERNAME);
            }

            return admin;
        } catch (Exception e) {
            log.error("初始化管理员账号失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "初始化管理员账号失败: " + e.getMessage());
        }
    }
}
