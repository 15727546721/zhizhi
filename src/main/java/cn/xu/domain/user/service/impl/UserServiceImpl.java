package cn.xu.domain.user.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.system.model.dto.user.UserRequest;
import cn.xu.api.web.model.dto.user.LoginRequest;
import cn.xu.api.web.model.dto.user.RegisterRequest;
import cn.xu.api.web.model.dto.user.UpdateUserReq;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.file.service.IFileStorageService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserRegisterEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.common.request.PageRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 负责用户相关的核心业务逻辑，包括用户信息的增删改查、认证授权等
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final IUserRepository userRepository;

    @Resource
    private IFileStorageService fileStorageService;

    @Override
    public UserEntity getUserById(Long userId) {
        if (userId == null) {
            log.error("[用户服务] 获取用户信息失败：用户ID为空");
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户ID不能为空");
        }

        try {
            log.info("[用户服务] 开始获取用户信息 - userId: {}", userId);
            return userRepository.findById(userId);
        } catch (Exception e) {
            log.error("[用户服务] 获取用户信息失败 - userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public String getNicknameById(Long userId) {
        return userRepository.getNicknameById(userId);
    }

    @Override
    public void update(UpdateUserReq user) {
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
        userEntity.setPhone(user.getPhone());
        userEntity.setUpdateTime(LocalDateTime.now());
        userRepository.update(userEntity);
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        try {
            log.info("[用户服务] 开始用户注册 - username: {}", request.getUsername());

            // 1. 验证用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户名已存在");
            }

            // 2. 创建用户实体
            UserRegisterEntity user = UserRegisterEntity.builder()
                    .username(request.getUsername())
                    .password(SaSecureUtil.sha256(request.getPassword()))
                    .nickname(request.getNickname())
                    .build();

            // 3. 注册用户
            long id = userRepository.register(user);
            log.info("[用户服务] 用户注册成功 - userId: {}", id);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[用户服务] 用户注册失败 - username: {}", request.getUsername(), e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "注册失败：" + e.getMessage());
        }
    }

    @Override
    public UserEntity login(LoginRequest request) {
        try {
            log.info("[用户服务] 用户登录请求 - email: {}", request.getEmail());

            // 1. 获取用户信息
            UserEntity user = userRepository.findByEmail(new Email(request.getEmail()))
                    .filter(u -> validatePassword(u, request.getPassword()))
                    .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户名或密码错误"));

            log.info("[用户服务] 用户登录成功 - userId: {}", user.getId());
            StpUtil.login(user.getId());
            return user;
        } catch (BusinessException e) {
            throw e;
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
    @Transactional
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
        user.setPhone(userInfo.getPhone());
        user.setUpdateTime(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = getUserInfo(userId);
        if (!validatePassword(user, oldPassword)) {
            throw new BusinessException("旧密码错误");
        }
//        user.setPassword(SaSecureUtil.sha256(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void banUser(Long userId) {
        UserEntity user = getUserInfo(userId);
        user.setStatus(1);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unbanUser(Long userId) {
        UserEntity user = getUserInfo(userId);
        user.setStatus(0);
        userRepository.save(user);
    }

    @Override
    public List<UserEntity> queryUserList(PageRequest pageRequest) {
        return userRepository.findAll(pageRequest.getPageNo(), pageRequest.getPageSize());
    }

    @Override
    @Transactional
    public void addUser(UserRequest userRequest) {
        validateUsernameAndEmail(userRequest.getUsername(), userRequest.getEmail());

        UserEntity user = UserEntity.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .nickname(userRequest.getNickname())
                .avatar(userRequest.getAvatar())
                .gender(userRequest.getGender())
                .phone(userRequest.getPhone())
                .region(userRequest.getRegion())
                .birthday(userRequest.getBirthday())
                .status(userRequest.getStatus())
                .description(userRequest.getDescription())
                .build();

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(UserRequest userRequest) {
        UserEntity user = getUserInfo(userRequest.getId());
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setNickname(userRequest.getNickname());
        user.setAvatar(userRequest.getAvatar());
        user.setGender(userRequest.getGender());
        user.setPhone(userRequest.getPhone());
        user.setRegion(userRequest.getRegion());
        user.setBirthday(userRequest.getBirthday());
        user.setStatus(userRequest.getStatus());
        user.setDescription(userRequest.getDescription());

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
//            user.setPassword(SaSecureUtil.sha256(userRequest.getPassword()));
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
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

            // 获取当前用户
            UserEntity currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
            }

            // 生成文件存储路径
            String fileName = String.format("avatar/%s/%s_%s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")),
                    currentUser.getId(),
                    file.getOriginalFilename()
            );

            // 上传新头像
            String avatarUrl = fileStorageService.uploadFile(file, fileName);

            // 获取旧头像URL
            String oldAvatarUrl = currentUser.getAvatar();

            // 更新用户头像URL
            currentUser.setAvatar(avatarUrl);
            userRepository.save(currentUser);

            // 如果存在旧头像且不是默认头像，则删除
            if (StringUtils.isNotEmpty(oldAvatarUrl)) {
                try {
                    fileStorageService.deleteFile(oldAvatarUrl);
                } catch (Exception e) {
                    // 删除旧头像失败不影响主流程，只记录日志
                    log.error("删除旧头像失败: {}", oldAvatarUrl, e);
                }
            }

            log.info("[用户服务] 用户头像上传成功 - url: {}", avatarUrl);
            return avatarUrl;
        } catch (Exception e) {
            log.error("[用户服务] 上传用户头像失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传头像失败：" + e.getMessage());
        }
    }

    @Override
    public Map<Long, UserEntity> getBatchUserInfo(Set<Long> userIds) {
        return userRepository.findByIds(userIds).stream()
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
            List<UserEntity> users = userRepository.findByIds(userIds);

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
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(new Email(email))) {
            throw new BusinessException("邮箱已被注册");
        }
    }

    private boolean validatePassword(UserEntity user, String password) {
//        return user.getPassword().equals(SaSecureUtil.sha256(password));
        return true;
    }

    private UserInfoEntity convertToUserInfoEntity(UserEntity user) {
        return UserInfoEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .phone(user.getPhone())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .description(user.getDescription())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    private UserEntity getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return getUserInfo(userId);
    }
} 