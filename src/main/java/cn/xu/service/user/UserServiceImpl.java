package cn.xu.service.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.cache.UserRankingCacheRepository;
import cn.xu.common.ResponseCode;
import cn.xu.common.request.PageRequest;
import cn.xu.controller.admin.model.dto.user.SysUserRequest;
import cn.xu.event.publisher.UserEventPublisher;
import cn.xu.integration.file.service.FileStorageService;
import cn.xu.model.dto.user.UpdateUserProfileRequest;
import cn.xu.model.dto.user.UserLoginRequest;
import cn.xu.model.dto.user.UserRegisterRequest;
import cn.xu.model.entity.User;
import cn.xu.repository.IUserRepository;
import cn.xu.service.security.LoginSecurityService;
import cn.xu.service.security.VerificationCodeService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 * <p>负责用户认证、资料管理、排行榜等功能</p>

 */
@Slf4j
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final UserEventPublisher userEventPublisher;
    private final UserRankingCacheRepository userRankingCacheRepository;
    private final UserSettingsService userSettingsService;
    private final VerificationCodeService verificationCodeService;
    private final LoginSecurityService loginSecurityService;

    // ==================== 认证相关 ====================

    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.debug("[用户] 获取当前登录用户ID失败", e);
        }
        return null;
    }

    /**
     * 用户注册
     *
     * <p>注册流程：</p>
     * <ol>
     *   <li>验证邮箱验证码</li>
     *   <li>检查邮箱是否已被注册</li>
     *   <li>创建用户并保存</li>
     *   <li>创建默认设置</li>
     * </ol>
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterRequest request) {
        if (request == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "注册信息不能为空");
        }

        try {
            // 1. 验证邮箱验证码
            if (request.getVerifyCode() != null && !request.getVerifyCode().isEmpty()) {
                verificationCodeService.verifyCodeOrThrow(
                        request.getEmail(),
                        request.getVerifyCode(),
                        VerificationCodeService.CodeScene.REGISTER);
            }

            // 2. 验证邮箱是否存在
            User existingUser = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (existingUser != null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "该邮箱已被注册");
            }

            // 3. 创建新用户
            User newUser = User.createNewUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getUsername(),
                    request.getNickname()
            );

            // 保存用户
            userRepository.save(newUser);

            // 创建默认用户设置
            try {
                userSettingsService.getOrCreateDefaultSettings(newUser.getId());
                log.info("为用户创建默认设置成功, userId: {}", newUser.getId());
            } catch (Exception e) {
                log.error("为用户创建默认设置失败, userId: {}", newUser.getId(), e);
                // 不抛出异常，因为设置创建失败不应影响注册流程
            }

            // 发布用户注册事件
            userEventPublisher.publishRegistered(newUser.getId(), newUser.getNickname());

            log.info("用户注册成功, email: {}, username: {}", request.getEmail(), newUser.getUsername());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "注册失败，请稍后重试");
        }
    }

    /**
     * 用户登录
     *
     * <p>登录流程：</p>
     * <ol>
     *   <li>检查账户是否被锁定</li>
     *   <li>验证邮箱和密码</li>
     *   <li>检查用户状态</li>
     *   <li>执行登录并更新登录时间</li>
     * </ol>
     *
     * @param request 登录请求
     * @return 登录成功的用户信息
     * @throws BusinessException 当登录失败时抛出
     */
    public User login(UserLoginRequest request) {
        return loginWithIp(request, null);
    }

    /**
     * 用户登录（带IP检查）
     *
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录成功的用户信息
     * @throws BusinessException 当登录失败时抛出
     */
    public User loginWithIp(UserLoginRequest request, String clientIp) {
        if (request == null || StringUtils.isBlank(request.getEmail()) || StringUtils.isBlank(request.getPassword())) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "邮箱和密码不能为空");
        }

        String email = request.getEmail().trim().toLowerCase();

        try {
            // 1. 登录前安全检查（账户锁定、IP封禁等）
            if (clientIp != null) {
                loginSecurityService.checkBeforeLogin(email, clientIp);
            }

            // 2. 验证用户登录信息 - 先根据邮箱查找用户，再验证密码
            User user = userRepository.findByEmailWithPassword(email).orElse(null);
            if (user == null) {
                // 记录登录失败
                if (clientIp != null) {
                    int remaining = loginSecurityService.recordLoginFailure(email, clientIp);
                    if (remaining > 0) {
                        throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(),
                                "邮箱或密码错误，剩余尝试次数：" + remaining);
                    }
                }
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "邮箱或密码错误");
            }

            // 3. 验证密码
            if (!user.verifyPassword(request.getPassword())) {
                // 记录登录失败
                if (clientIp != null) {
                    int remaining = loginSecurityService.recordLoginFailure(email, clientIp);
                    if (remaining > 0) {
                        throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(),
                                "邮箱或密码错误，剩余尝试次数：" + remaining);
                    } else {
                        throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(),
                                "登录失败次数过多，账户已被锁定");
                    }
                }
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(), "邮箱或密码错误");
            }

            // 4. 检查用户状态
            if (user.getStatus() != User.STATUS_NORMAL) {
                throw new BusinessException(ResponseCode.USER_DISABLED.getCode(), "用户已被禁用");
            }

            // 5. 登录成功，清除失败记录
            loginSecurityService.clearLoginFailure(email);

            // 6. 执行登录
            StpUtil.login(user.getId());

            // 7. 更新最后登录时间和登录IP
            user.setLastLoginTime(LocalDateTime.now());
            if (clientIp != null) {
                user.setLastLoginIp(clientIp);
            }
            userRepository.save(user);

            log.info("用户登录成功, userId: {}, email: {}, ip: {}", user.getId(), user.getEmail(), clientIp);
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败, email: {}", email, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "登录失败，请稍后重试");
        }
    }

    /**
     * 验证码登录
     *
     * <p>登录流程：</p>
     * <ol>
     *   <li>验证邮箱验证码</li>
     *   <li>检查用户是否存在</li>
     *   <li>检查用户状态</li>
     *   <li>执行登录并更新登录时间</li>
     * </ol>
     *
     * @param email    邮箱
     * @param code     验证码
     * @param clientIp 客户端IP
     * @return 登录成功的用户信息
     * @throws BusinessException 当登录失败时抛出
     */
    public User loginWithCode(String email, String code, String clientIp) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(code)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "邮箱和验证码不能为空");
        }

        String trimmedEmail = email.trim().toLowerCase();

        try {
            // 1. 验证验证码
            verificationCodeService.verifyCodeOrThrow(trimmedEmail, code, VerificationCodeService.CodeScene.LOGIN);

            // 2. 查找用户
            User user = userRepository.findByEmail(trimmedEmail).orElse(null);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在，请先注册");
            }

            // 3. 检查用户状态
            if (user.getStatus() != User.STATUS_NORMAL) {
                throw new BusinessException(ResponseCode.USER_DISABLED.getCode(), "用户已被禁用");
            }

            // 4. 执行登录
            StpUtil.login(user.getId());

            // 5. 更新最后登录时间和登录IP
            user.setLastLoginTime(LocalDateTime.now());
            if (clientIp != null) {
                user.setLastLoginIp(clientIp);
            }
            userRepository.save(user);

            log.info("验证码登录成功, userId: {}, email: {}, ip: {}", user.getId(), user.getEmail(), clientIp);
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证码登录失败, email: {}", trimmedEmail, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "登录失败，请稍后重试");
        }
    }

    /**
     * 用户名登录（管理员登录）
     */
    public User loginByUsername(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户名和密码不能为空");
        }

        try {
            // 根据用户名查找用户
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户名或密码错误");
            }

            // 验证密码
            if (!user.verifyPassword(password)) {
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(), "用户名或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != User.STATUS_NORMAL) {
                throw new BusinessException(ResponseCode.USER_DISABLED.getCode(), "用户已被禁用");
            }

            // 执行登录
            StpUtil.login(user.getId());

            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);

            log.info("用户名登录成功, userId: {}, username: {}", user.getId(), user.getUsername());
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户名登录失败, username: {}", username, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "登录失败，请稍后重试");
        }
    }

    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }

            // 验证旧密码
            if (!user.verifyPassword(oldPassword)) {
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(), "原密码错误");
            }

            // 更新密码
            user.updatePassword(newPassword);
            userRepository.save(user);

            log.info("用户修改密码成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改密码失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "修改密码失败，请稍后重试");
        }
    }

    // ==================== 用户管理 ====================

    /**
     * 获取用户信息
     */
    public User getUserInfo(Long userId) {
        if (userId == null) {
            return null;
        }
        return getUserById(userId);
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息，不存在返回null
     */
    public User getUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }

        try {
            return userRepository.findById(userId).orElseThrow(
                    () -> new BusinessException(ResponseCode.NOT_FOUND.getCode(), "用户不存在")
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据用户ID获取用户信息失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取用户信息失败");
        }
    }

    /**
     * 根据用户ID获取用户昵称
     */
    public String getNicknameById(Long userId) {
        User user = getUserById(userId);
        return user != null ? user.getNickname() : null;
    }

    /**
     * 批量获取用户信息（List参数返回Map）
     */
    public Map<Long, User> batchGetUserInfo(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            Set<Long> userIdSet = new HashSet<>(userIds);
            List<User> users = userRepository.findByIds(new ArrayList<>(userIdSet));
            return users.stream().collect(Collectors.toMap(User::getId, user -> user, (existing, replacement) -> existing));
        } catch (Exception e) {
            log.error("批量获取用户信息失败, userIds: {}", userIds, e);
            return new HashMap<>();
        }
    }

    /**
     * 批量获取用户信息（Set参数返回List）
     */
    public List<User> batchGetUsersByIds(Set<Long> userIds) {
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
     * 批量获取用户信息Map
     */
    public Map<Long, User> getBatchUserInfo(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        try {
            List<User> users = userRepository.findByIds(new ArrayList<>(userIds));
            return users.stream()
                    .collect(Collectors.toMap(User::getId, user -> user, (existing, replacement) -> existing));
        } catch (Exception e) {
            log.error("批量获取用户信息Map失败, userIds: {}", userIds, e);
            return new HashMap<>();
        }
    }

    /**
     * 批量获取用户信息Map
     */
    public Map<Long, User> getUserMapByIds(Set<Long> userIds) {
        return getBatchUserInfo(userIds);
    }

    // ==================== 用户资料管理 ====================

    /**
     * 更新用户资料
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        if (userId == null || request == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }

            // 更新用户资料
            boolean updated = false;

            if (StringUtils.isNotBlank(request.getNickname())) {
                user.setNickname(request.getNickname().trim());
                updated = true;
            }

            if (StringUtils.isNotBlank(request.getAvatar())) {
                user.setAvatar(request.getAvatar());
                updated = true;
            }

            if (StringUtils.isNotBlank(request.getDescription())) {
                user.setDescription(request.getDescription());
                updated = true;
            }

            if (request.getGender() != null) {
                user.setGender(request.getGender());
                updated = true;
            }

            if (updated) {
                user.setUpdateTime(LocalDateTime.now());
                userRepository.save(user);

                log.info("更新用户资料成功, userId: {}", userId);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户资料失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新用户资料失败，请稍后重试");
        }
    }

    /**
     * 上传用户头像
     */
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "头像文件不能为空");
        }

        try {
            // 生成安全的文件路径（格式: yyyyMM/uuid.ext）
            String fileName = cn.xu.common.constants.FilePathConstants.buildPath(
                    file.getOriginalFilename());

            // 上传到文件存储服务（返回的是文件名）
            String storedFileName = fileStorageService.uploadFile(file, fileName);
            // 获取完整的访问URL
            String avatarUrl = fileStorageService.getFileUrl(storedFileName);
            log.info("上传用户头像成功, storedFileName: {}, avatarUrl: {}", storedFileName, avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("上传用户头像失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传头像失败: " + e.getMessage());
        }
    }

    /**
     * 更新用户头像
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUserAvatar(Long userId, String avatarUrl) {
        if (userId == null || StringUtils.isBlank(avatarUrl)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }

            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);

            log.info("更新用户头像成功, userId: {}, avatar: {}", userId, avatarUrl);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户头像失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新头像失败，请稍后重试");
        }
    }

    // ==================== 用户状态管理 ====================

    /**
     * 禁止用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }

            user.setStatus(User.STATUS_DISABLED);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);

            // 踢下线
            StpUtil.kickout(userId);

            log.info("禁止用户成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("禁止用户失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "禁止用户失败，请稍后重试");
        }
    }

    /**
     * 解封用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }

            user.setStatus(User.STATUS_NORMAL);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);

            log.info("解封用户成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解封用户失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "解封用户失败，请稍后重试");
        }
    }

    // ==================== 管理员功能 ====================

    /**
     * 查询用户列表
     */
    public List<User> queryUserList(PageRequest pageRequest) {
        if (pageRequest == null) {
            pageRequest = PageRequest.of(1, 20);
        }

        try {
            return userRepository.findByPage(pageRequest.getPageNo(), pageRequest.getPageSize());
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 带条件查询用户列表
     */
    public List<User> queryUserListWithFilters(String username, Integer status, Integer userType, Integer page, Integer size) {
        try {
            if (page == null || page < 1) page = 1;
            if (size == null || size < 1) size = 10;
            return userRepository.findByConditions(username, status, userType, page, size);
        } catch (Exception e) {
            log.error("带条件查询用户列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 统计带条件的用户数量
     */
    public Long countUsersByFilters(String username, Integer status, Integer userType) {
        try {
            return userRepository.countByConditions(username, status, userType);
        } catch (Exception e) {
            log.error("统计用户数量失败", e);
            return 0L;
        }
    }

    /**
     * 添加用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUserRequest userRequest) {
        if (userRequest == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户信息不能为空");
        }

        try {
            // 检查邮箱是否已存在
            if (StringUtils.isNotBlank(userRequest.getEmail())) {
                User existingUser = userRepository.findByEmail(userRequest.getEmail()).orElse(null);
                if (existingUser != null) {
                    throw new BusinessException(ResponseCode.USER_ALREADY_EXISTS.getCode(), "该邮箱已被使用");
                }
            }

            // 创建用户
            User newUser = User.createNewUser(
                    userRequest.getEmail(),
                    userRequest.getPassword(),
                    userRequest.getUsername(),
                    userRequest.getNickname()
            );

            // 设置附加属性
            if (StringUtils.isNotBlank(userRequest.getPhone())) {
                newUser.setPhone(userRequest.getPhone());
            }
            if (StringUtils.isNotBlank(userRequest.getAvatar())) {
                newUser.setAvatar(userRequest.getAvatar());
            }
            if (userRequest.getGender() != null) {
                newUser.setGender(userRequest.getGender());
            }
            if (StringUtils.isNotBlank(userRequest.getDescription())) {
                newUser.setDescription(userRequest.getDescription());
            }
            if (userRequest.getUserType() != null) {
                newUser.setUserType(userRequest.getUserType());
            }
            if (userRequest.getStatus() != null) {
                newUser.setStatus(userRequest.getStatus());
            }

            userRepository.save(newUser);
            log.info("管理员添加用户成功, email: {}", userRequest.getEmail());
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
    public void updateUser(SysUserRequest userRequest) {
        if (userRequest == null || userRequest.getId() == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户信息不能为空");
        }

        try {
            User user = getUserById(userRequest.getId());
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }

            // 更新用户信息 - 支持所有可编辑字段
            if (StringUtils.isNotBlank(userRequest.getUsername())) {
                user.setUsername(userRequest.getUsername());
            }
            if (StringUtils.isNotBlank(userRequest.getNickname())) {
                user.setNickname(userRequest.getNickname());
            }
            if (StringUtils.isNotBlank(userRequest.getEmail())) {
                // 检查邮箱是否已被其他用户使用
                User existingUser = userRepository.findByEmail(userRequest.getEmail()).orElse(null);
                if (existingUser != null && !existingUser.getId().equals(userRequest.getId())) {
                    throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "该邮箱已被其他用户使用");
                }
                user.setEmail(userRequest.getEmail());
            }
            if (StringUtils.isNotBlank(userRequest.getPhone())) {
                user.setPhone(userRequest.getPhone());
            }
            if (StringUtils.isNotBlank(userRequest.getAvatar())) {
                user.setAvatar(userRequest.getAvatar());
            }
            if (userRequest.getGender() != null) {
                user.setGender(userRequest.getGender());
            }
            if (StringUtils.isNotBlank(userRequest.getDescription())) {
                user.setDescription(userRequest.getDescription());
            }
            if (userRequest.getUserType() != null) {
                user.setUserType(userRequest.getUserType());
            }
            if (userRequest.getStatus() != null) {
                user.setStatus(userRequest.getStatus());
            }

            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);

            log.info("管理员更新用户成功, userId: {}", userRequest.getId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员更新用户失败, userId: {}", userRequest.getId(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新用户失败，请稍后重试");
        }
    }

    /**
     * 删除用户（管理员操作）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }

            // 软删除：设置状态为已删除
            user.setStatus(User.STATUS_DELETED);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);

            // 踢下线
            StpUtil.kickout(userId);

            log.info("管理员删除用户成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("管理员删除用户失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除用户失败，请稍后重试");
        }
    }

    /**
     * 批量删除用户（管理员操作）
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        try {
            for (Long userId : userIds) {
                User user = getUserById(userId);
                if (user != null) {
                    user.setStatus(User.STATUS_DELETED);
                    user.setUpdateTime(LocalDateTime.now());
                    userRepository.save(user);
                    StpUtil.kickout(userId);
                }
            }
            log.info("批量删除用户成功, userIds: {}", userIds);
        } catch (Exception e) {
            log.error("批量删除用户失败, userIds: {}", userIds, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量删除用户失败");
        }
    }

    /**
     * 用户注销账户（需要验证密码）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccountWithPassword(Long userId, String password) {
        if (userId == null || StringUtils.isBlank(password)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }

        try {
            User user = getUserById(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }

            // 验证密码
            if (!user.verifyPassword(password)) {
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(), "密码错误");
            }

            // 软删除：设置状态为已删除
            user.setStatus(User.STATUS_DELETED);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);

            // 踢下线
            StpUtil.kickout(userId);

            log.info("用户注销账户成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注销账户失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "注销账户失败，请稍后重试");
        }
    }

    // ==================== 排行榜功能 ====================

    /**
     * 查询用户排行榜
     */
    public List<User> findUserRanking(String sortType, int page, int size) {
        try {
            // 先从缓存获取用户ID列表
            int start = (page - 1) * size;
            int end = start + size - 1;
            List<Long> cachedUserIds = userRankingCacheRepository.getUserRankingIds(sortType, start, end);
            if (cachedUserIds != null && !cachedUserIds.isEmpty()) {
                // 根据ID获取用户信息
                return userRepository.findByIds(new ArrayList<>(cachedUserIds));
            }

            // 缓存未命中，从数据库查询
            int offset = Math.max(0, (page - 1) * size);
            List<User> users = userRepository.findUserRanking(sortType, offset, size);

            // 写入缓存 - 转换为用户ID和分数的映射
            if (users != null && !users.isEmpty()) {
                Map<Long, Double> userScores = new HashMap<>();
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    // 根据排序类型计算分数
                    double score = calculateRankingScore(user, sortType);
                    userScores.put(user.getId(), score);
                }
                userRankingCacheRepository.cacheUserRanking(sortType, userScores);
            }

            return users != null ? users : new ArrayList<>();
        } catch (Exception e) {
            log.error("查询用户排行榜失败, sortType: {}, page: {}, size: {}", sortType, page, size, e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算用户排行榜分数
     */
    private double calculateRankingScore(User user, String sortType) {
        if (user == null) return 0.0;
        switch (sortType) {
            case "fans":
                return user.getFansCount() != null ? user.getFansCount().doubleValue() : 0.0;
            case "likes":
                return user.getLikeCount() != null ? user.getLikeCount().doubleValue() : 0.0;
            case "posts":
                return user.getPostCount() != null ? user.getPostCount().doubleValue() : 0.0;
            case "comprehensive":
            default:
                // 综合分数 = 粉丝数*1 + 获赞数*0.5 + 帖子数*2
                double fans = user.getFansCount() != null ? user.getFansCount() : 0.0;
                double likes = user.getLikeCount() != null ? user.getLikeCount() : 0.0;
                double posts = user.getPostCount() != null ? user.getPostCount() : 0.0;
                return fans + likes * 0.5 + posts * 2;
        }
    }

    /**
     * 统计用户总数
     */
    public Long countAllUsers() {
        try {
            return userRepository.countAllUsers();
        } catch (Exception e) {
            log.error("统计用户总数失败", e);
            return 0L;
        }
    }

    /**
     * 初始化/重置管理员账号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User initAdminUser() {
        final String ADMIN_USERNAME = "admin";
        final String ADMIN_PASSWORD = "AdminPassword123!";

        try {
            // 查找是否存在admin用户
            User admin = userRepository.findByUsername(ADMIN_USERNAME).orElse(null);

            if (admin == null) {
                // 创建新的admin用户
                admin = new User();
                admin.setUsername(ADMIN_USERNAME);
                admin.setNickname("系统管理员");
                admin.setEmail("admin@zhizhi.com");
                admin.setUserType(3); // 3=管理员
                admin.setStatus(User.STATUS_NORMAL);
                admin.setPassword(ADMIN_PASSWORD);
                admin.encryptPassword(); // 加密密码
                admin.setCreateTime(LocalDateTime.now());
                admin.setUpdateTime(LocalDateTime.now());
                userRepository.save(admin);
                log.info("创建管理员账号成功: {}", ADMIN_USERNAME);
            } else {
                // 重置admin用户密码和状态
                admin.setPassword(ADMIN_PASSWORD);
                admin.encryptPassword(); // 重新加密密码
                admin.setUserType(3); // 确保是管理员
                admin.setStatus(User.STATUS_NORMAL); // 确保未被禁用
                admin.setUpdateTime(LocalDateTime.now());
                userRepository.save(admin);
                log.info("重置管理员账号密码成功: {}", ADMIN_USERNAME);
            }

            return admin;
        } catch (Exception e) {
            log.error("初始化管理员账号失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "初始化管理员账号失败: " + e.getMessage());
        }
    }

    /**
     * 注销账户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long userId, String password) {
        deleteAccountWithPassword(userId, password);
    }

    /**
     * 搜索用户（支持@提及自动补全）
     *
     * @param keyword 关键词（用户名或昵称）
     * @param limit 限制数量
     * @return 用户列表
     */
    public List<User> searchUsers(String keyword, Integer limit) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return userRepository.searchUsers(keyword.trim(), limit);
        } catch (Exception e) {
            log.error("搜索用户失败: keyword={}", keyword, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息Map
     */
    @Override
    public Map<Long, User> findUserInfo(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        return getBatchUserInfo(userIds);
    }
}