package cn.xu.domain.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.system.model.dto.user.SysUserRequest;
import cn.xu.api.web.model.dto.user.UpdateUserRequest;
import cn.xu.api.web.model.dto.user.UpdateUserProfileRequest;
import cn.xu.api.web.model.dto.user.UserLoginRequest;
import cn.xu.api.web.model.dto.user.UserRegisterRequest;
import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.request.PageRequest;
import cn.xu.domain.file.service.IFileStorageService;
import cn.xu.domain.user.event.UserEventPublisher;
import cn.xu.domain.user.event.UserLoggedInEvent;
import cn.xu.domain.user.event.UserRegisteredEvent;
import cn.xu.domain.user.event.UserUpdatedEvent;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.model.valobj.Password;
import cn.xu.domain.user.model.valobj.Phone;
import cn.xu.domain.user.model.valobj.Username;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.cache.UserRankingCacheRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 负责用户相关的核心业务逻辑，包括用户信息的增删改查、认证授权等
 * 
 * @author xu
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final IUserRepository userRepository;
    private final IFileStorageService fileStorageService;
    private final UserEventPublisher userEventPublisher;
    private final UserRankingCacheRepository userRankingCacheRepository;

    @Override
    public UserEntity getUserById(Long userId) {
        if (userId == null) {
            log.error("[用户服务] 获取用户信息失败：用户ID为空");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户ID不能为空");
        }

        try {
            log.info("[用户服务] 开始获取用户信息 - userId: {}", userId);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在"));
        } catch (Exception e) {
            log.error("[用户服务] 获取用户信息失败 - userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public String getNicknameById(Long userId) {
        return getUserById(userId).getNickname();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateUserRequest user) {
        UserEntity userEntity = getUserById(user.getId());
        if (userEntity == null) {
            log.error("[用户服务] 更新用户信息失败：用户不存在");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }
        userEntity.setNickname(user.getNickname());
        userEntity.setAvatar(user.getAvatar());
        userEntity.setGender(user.getGender());
        userEntity.setRegion(user.getRegion());
        userEntity.setBirthday(user.getBirthday());
        userEntity.setDescription(user.getDescription());
        userEntity.setPhone(user.getPhone() != null ? new Phone(user.getPhone()) : null);
        userEntity.setUpdateTime(LocalDateTime.now());
        userRepository.update(userEntity);
        
        // 发布用户更新事件
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .userId(userEntity.getId())
                .username(userEntity.getUsernameValue())
                .updateTime(LocalDateTime.now())
                .build();
        userEventPublisher.publishUserUpdated(event);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        try {
            log.info("[用户服务] 开始更新用户资料 - userId: {}", userId);
            
            // 获取用户实体
            UserEntity userEntity = getUserById(userId);
            if (userEntity == null) {
                log.error("[用户服务] 更新用户资料失败：用户不存在 - userId: {}", userId);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
            }
            
            // 更新可修改的字段（使用领域方法，符合DDD原则）
            if (request.getNickname() != null) {
                userEntity.setNickname(request.getNickname());
            }
            if (request.getAvatar() != null) {
                userEntity.setAvatar(request.getAvatar());
            }
            if (request.getGender() != null) {
                userEntity.setGender(request.getGender());
            }
            if (request.getRegion() != null) {
                userEntity.setRegion(request.getRegion());
            }
            if (request.getBirthday() != null) {
                userEntity.setBirthday(request.getBirthday());
            }
            if (request.getDescription() != null) {
                userEntity.setDescription(request.getDescription());
            }
            if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
                userEntity.setPhone(new Phone(request.getPhone()));
            }
            
            // 更新修改时间
            userEntity.setUpdateTime(LocalDateTime.now());
            
            // 保存更新
            userRepository.update(userEntity);
            
            log.info("[用户服务] 用户资料更新成功 - userId: {}", userId);
            
            // 发布用户更新事件
            UserUpdatedEvent event = UserUpdatedEvent.builder()
                    .userId(userEntity.getId())
                    .username(userEntity.getUsernameValue())
                    .updateTime(LocalDateTime.now())
                    .build();
            userEventPublisher.publishUserUpdated(event);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[用户服务] 更新用户资料失败 - userId: {}, request: {}", userId, request, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新用户资料失败：" + e.getMessage());
        }
    }

    @Override
    public List<UserEntity> batchGetUserInfo(List<Long> userIds) {
        return userRepository.findByIds(userIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterRequest request) {
        try {
            log.info("[用户服务] 开始用户注册 - username: {}", request.getUsername());

            // 1. 验证用户名和邮箱是否已存在
            Username username = new Username(request.getUsername());
            Email email = new Email(request.getEmail());
            
            if (userRepository.existsByUsername(username)) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户名已存在");
            }
            
            if (userRepository.existsByEmail(email)) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "邮箱已被注册");
            }

            // 2. 创建用户实体
            UserEntity user = UserEntity.createNewUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail(),
                    request.getNickname()
            );

            // 3. 保存用户
            UserEntity savedUser = userRepository.save(user);
            log.info("[用户服务] 用户注册成功 - userId: {}", savedUser.getId());
            
            // 4. 发布用户注册事件
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(savedUser.getId())
                    .username(savedUser.getUsernameValue())
                    .email(savedUser.getEmailValue())
                    .registerTime(LocalDateTime.now())
                    .build();
            userEventPublisher.publishUserRegistered(event);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[用户服务] 用户注册失败 - username: {}", request.getUsername(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "注册失败：" + e.getMessage());
        }
    }

    @Override
    public UserEntity login(UserLoginRequest request) {
        try {
            log.info("[用户服务] 用户登录请求 - email: {}", request.getEmail());

            // 1. 验证登录参数
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "邮箱不能为空");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "密码不能为空");
            }

            // 2. 根据邮箱查找用户（包含密码信息）
            Email email = new Email(request.getEmail());
            UserEntity user = userRepository.findByEmailWithPassword(email)
                    .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在"));

            // 3. 验证密码
            if (!validatePassword(user, request.getPassword())) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户名或密码错误");
            }

            // 4. 验证用户状态
            user.validateCanLogin();

            log.info("[用户服务] 用户登录成功 - userId: {}", user.getId());
            StpUtil.login(user.getId());
            
            // 发布用户登录事件
            UserLoggedInEvent event = UserLoggedInEvent.builder()
                    .userId(user.getId())
                    .username(user.getUsernameValue())
                    .loginTime(LocalDateTime.now())
                    .build();
            userEventPublisher.publishUserLoggedIn(event);
            
            return user;
        } catch (Exception e) {
            log.error("[用户服务] 用户登录失败 - email: {}", request.getEmail(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "登录失败：" + e.getMessage());
        }
    }

    @Override
    public UserEntity getUserInfo(Long userId) {
        UserEntity user = getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfoEntity userInfoEntity) {
        try {
            log.info("[用户服务] 开始更新用户信息 - userId: {}", userInfoEntity.getId());

            // 1. 获取当前用户
            Long userId = StpUtil.getLoginIdAsLong();
            UserEntity user = getUserInfo(userId);

            // 2. 更新用户信息
            updateUserFields(user, userInfoEntity);

            // 3. 保存更新
            userRepository.save(user);
            log.info("[用户服务] 用户信息更新成功 - userId: {}", userId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[用户服务] 更新用户信息失败 - userInfo: {}", userInfoEntity, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户字段信息
     *
     * @param user     用户实体
     * @param userInfo 用户信息实体
     */
    private void updateUserFields(UserEntity user, UserInfoEntity userInfo) {
        user.setNickname(userInfo.getNickname());
        user.setAvatar(userInfo.getAvatar());
        user.setGender(userInfo.getGender());
        user.setRegion(userInfo.getRegion());
        user.setBirthday(userInfo.getBirthday());
        user.setDescription(userInfo.getDescription());
        user.setPhone(userInfo.getPhone() != null ? new Phone(userInfo.getPhone()) : null);
        user.setUpdateTime(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = getUserInfo(userId);
        if (!validatePassword(user, oldPassword)) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(new Password(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId) {
        UserEntity user = getUserInfo(userId);
        user.setStatus(UserEntity.UserStatus.fromCode(1));
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(Long userId) {
        UserEntity user = getUserInfo(userId);
        user.setStatus(UserEntity.UserStatus.fromCode(0));
        userRepository.save(user);
    }

    @Override
    public List<UserEntity> queryUserList(PageRequest pageRequest) {
        return userRepository.findByPage(pageRequest.getPageNo(), pageRequest.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUserRequest userRequest) {
        validateUsernameAndEmail(userRequest.getUsername(), userRequest.getEmail());

        UserEntity user = UserEntity.builder()
                .username(userRequest.getUsername() != null ? new Username(userRequest.getUsername()) : null)
                .password(userRequest.getPassword() != null ? new Password(userRequest.getPassword()) : null)
                .email(userRequest.getEmail() != null ? new Email(userRequest.getEmail()) : null)
                .nickname(userRequest.getNickname())
                .avatar(userRequest.getAvatar())
                .gender(userRequest.getGender())
                .phone(userRequest.getPhone() != null ? new Phone(userRequest.getPhone()) : null)
                .region(userRequest.getRegion())
                .birthday(userRequest.getBirthday())
                .status(UserEntity.UserStatus.fromCode(userRequest.getStatus()))
                .description(userRequest.getDescription())
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserRequest userRequest) {
        UserEntity user = getUserInfo(userRequest.getId());
        user.setUsername(userRequest.getUsername() != null ? new Username(userRequest.getUsername()) : null);
        user.setEmail(userRequest.getEmail() != null ? new Email(userRequest.getEmail()) : null);
        user.setNickname(userRequest.getNickname());
        user.setAvatar(userRequest.getAvatar());
        user.setGender(userRequest.getGender());
        user.setPhone(userRequest.getPhone() != null ? new Phone(userRequest.getPhone()) : null);
        user.setRegion(userRequest.getRegion());
        user.setBirthday(userRequest.getBirthday());
        user.setStatus(UserEntity.UserStatus.fromCode(userRequest.getStatus()));
        user.setDescription(userRequest.getDescription());

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(new Password(userRequest.getPassword()));
        }

        userRepository.save(user);
        
        // 发布用户更新事件
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .userId(user.getId())
                .username(user.getUsernameValue())
                .updateTime(LocalDateTime.now())
                .build();
        userEventPublisher.publishUserUpdated(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public UserInfoEntity queryUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserEntity user = getUserInfo(userId);
        return convertToUserInfoEntity(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile file) {
        try {
            log.info("[用户服务] 开始上传用户头像 - fileName: {}, size: {}",
                    file.getOriginalFilename(), file.getSize());

            UserEntity currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isEmpty()) {
                originalFileName = "avatar.jpg";
            }
            
            String extension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalFileName.substring(lastDotIndex);
            } else {
                String contentType = file.getContentType();
                if (contentType != null) {
                    if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                        extension = ".jpg";
                    } else if (contentType.contains("png")) {
                        extension = ".png";
                    } else if (contentType.contains("gif")) {
                        extension = ".gif";
                    } else {
                        extension = ".jpg";
                    }
                } else {
                    extension = ".jpg";
                }
            }
            
            String safeFileName = String.format("avatar/%s/%s_%d%s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")),
                    currentUser.getId(),
                    System.currentTimeMillis(),
                    extension
            );

            String avatarUrl = fileStorageService.uploadFile(file, safeFileName);
            String oldAvatarUrl = currentUser.getAvatar();

            currentUser.setAvatar(avatarUrl);
            userRepository.save(currentUser);

            if (StringUtils.isNotEmpty(oldAvatarUrl) && !oldAvatarUrl.equals("default-avatar.png")) {
                try {
                    fileStorageService.deleteFile(oldAvatarUrl);
                } catch (Exception e) {
                    log.error("删除旧头像失败: {}", oldAvatarUrl, e);
                }
            }

            log.info("[用户服务] 用户头像上传成功 - imageUrl: {}", avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("[用户服务] 上传用户头像失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传头像失败：" + e.getMessage());
        }
    }

    @Override
    public Map<Long, UserEntity> getBatchUserInfo(Set<Long> userIds) {
        List<Long> collect = new ArrayList<>(userIds);
        return userRepository.findByIds(collect).stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));
    }

    /**
     * 批量获取用户信息Map
     *
     * @param userIds 用户ID集合
     * @return 用户信息Map, key为用户ID, value为用户信息
     */
    @Override
    public Map<Long, UserEntity> getUserMapByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            log.warn("[用户服务] 批量获取用户信息：用户ID集合为空");
            return Collections.emptyMap();
        }

        try {
            log.info("[用户服务] 开始批量获取用户信息 - userIds: {}", userIds);
            List<Long> collect = new ArrayList<>(userIds);
            List<UserEntity> users = userRepository.findByIds(collect);

            Map<Long, UserEntity> userMap = users.stream()
                    .collect(Collectors.toMap(UserEntity::getId, user -> user));

            log.debug("[用户服务] 批量获取用户信息成功 - 请求数量: {}, 实际获取: {}", userIds.size(), users.size());
            return userMap;
        } catch (Exception e) {
            log.error("[用户服务] 批量获取用户信息失败 - userIds: {}", userIds, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "批量获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public Long getCurrentUserId() {
        try {
            // 从当前上下文获取用户ID的具体实现
            Long userId = StpUtil.getLoginIdAsLong();
            log.debug("[用户服务] 获取当前用户ID - userId: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("[用户服务] 获取当前用户ID失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取当前用户信息失败");
        }
    }

    private void validateUsernameAndEmail(String username, String email) {
        Username usernameObj = new Username(username);
        Email emailObj = new Email(email);
        
        if (userRepository.existsByUsername(usernameObj)) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(emailObj)) {
            throw new BusinessException("邮箱已被注册");
        }
    }

    private boolean validatePassword(UserEntity user, String password) {
        return user.validatePassword(password);
    }

    private UserInfoEntity convertToUserInfoEntity(UserEntity user) {
        return UserInfoEntity.builder()
                .id(user.getId())
                .username(user.getUsernameValue())
                .email(user.getEmailValue())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .phone(user.getPhoneValue())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .description(user.getDescription())
                .status(user.getStatusCode())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    private UserEntity getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getUserInfo(userId);
    }
    
    @Override
    public List<UserEntity> findUserRanking(String sortType, int page, int size) {
        try {
            log.info("[用户服务] 开始查询用户排行榜 - sortType: {}, page: {}, size: {}", sortType, page, size);
            
            // 参数校验
            if (page < 1) {
                page = 1;
            }
            if (size < 1) {
                size = 10;
            }
            if (size > 100) {
                size = 100; // 限制最大返回数量
            }
            
            // 默认排序类型
            if (sortType == null || sortType.isEmpty()) {
                sortType = "fans";
            }
            
            // 先尝试从Redis缓存获取
            int start = (page - 1) * size;
            int end = start + size - 1;
            List<Long> userIds = userRankingCacheRepository.getUserRankingIds(sortType, start, end);
            
            if (userIds != null && !userIds.isEmpty()) {
                // 缓存命中，根据ID批量查询用户信息
                log.debug("[用户服务] 从Redis缓存获取用户排行榜ID: size={}", userIds.size());
                List<UserEntity> users = userRepository.findByIds(userIds);
                
                // 保持排序顺序（Redis返回的顺序）
                Map<Long, UserEntity> userMap = new HashMap<>();
                for (UserEntity user : users) {
                    if (user != null && user.getId() != null) {
                        userMap.put(user.getId(), user);
                    }
                }
                
                List<UserEntity> sortedUsers = new ArrayList<>();
                for (Long userId : userIds) {
                    UserEntity user = userMap.get(userId);
                    if (user != null) {
                        sortedUsers.add(user);
                    }
                }
                
                log.info("[用户服务] 从缓存查询用户排行榜成功 - 返回数量: {}", sortedUsers.size());
                return sortedUsers;
            }
            
            // 缓存未命中，从数据库查询
            log.debug("[用户服务] Redis缓存未命中，从数据库查询用户排行榜");
            int offset = (page - 1) * size;
            List<UserEntity> users = userRepository.findUserRanking(sortType, offset, size);
            
            log.info("[用户服务] 从数据库查询用户排行榜成功 - 返回数量: {}", users.size());
            return users;
        } catch (Exception e) {
            log.error("[用户服务] 查询用户排行榜失败 - sortType: {}, page: {}, size: {}", sortType, page, size, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "查询用户排行榜失败：" + e.getMessage());
        }
    }
    
    @Override
    public Long countAllUsers() {
        try {
            log.info("[用户服务] 开始统计用户总数");
            Long count = userRepository.countAllUsers();
            log.info("[用户服务] 统计用户总数成功 - 总数: {}", count);
            return count;
        } catch (Exception e) {
            log.error("[用户服务] 统计用户总数失败", e);
            return 0L;
        }
    }
}