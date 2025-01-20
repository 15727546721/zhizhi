package cn.xu.domain.user.service.impl;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.controller.user.LoginRequest;
import cn.xu.api.web.controller.user.RegisterRequest;
import cn.xu.api.web.model.dto.common.PageRequest;
import cn.xu.api.web.model.dto.user.UserRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.file.service.IFileStorageService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valueobject.Email;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserService;
import cn.xu.exception.BusinessException;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final IUserRepository userRepository;

    @Resource
    private IFileStorageService fileStorageService;

    @Override
    @Transactional
    public UserEntity register(RegisterRequest request) {
        validateUsernameAndEmail(request.getUsername(), request.getEmail());

        UserEntity user = createUserEntity(request);
        return userRepository.save(user);
    }

    @Override
    public UserEntity login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(new Email(request.getEmail()))
                .filter(u -> validatePassword(u, request.getPassword()))
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        StpUtil.login(user.getId());
        return user;
    }

    @Override
    public UserEntity getUserInfo(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    @Override
    @Transactional
    public UserEntity updateUserInfo(Long userId, String nickname, String avatar,
                                     Integer gender, String region, String birthday,
                                     String description) {
        UserEntity user = getUserInfo(userId);
        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setGender(gender);
        user.setRegion(region);
        user.setBirthday(birthday);
        user.setDescription(description);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = getUserInfo(userId);
        if (!validatePassword(user, oldPassword)) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(SaSecureUtil.sha256(newPassword));
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
                .password(SaSecureUtil.sha256(userRequest.getPassword()))
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
            user.setPassword(SaSecureUtil.sha256(userRequest.getPassword()));
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
    @Transactional
    public void updateUserInfo(UserInfoEntity userInfoEntity) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserEntity user = getUserInfo(userId);

        user.setNickname(userInfoEntity.getNickname());
        user.setAvatar(userInfoEntity.getAvatar());
        user.setGender(userInfoEntity.getGender());
        user.setPhone(userInfoEntity.getPhone());
        user.setRegion(userInfoEntity.getRegion());
        user.setBirthday(userInfoEntity.getBirthday());
        user.setDescription(userInfoEntity.getDescription());

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(MultipartFile file) {
        // 获取当前用户
        UserEntity currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
        }

        try {
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

            return avatarUrl;
        } catch (Exception e) {
            log.error("上传头像失败", e);
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
            return new HashMap<>();
        }
        
        // 批量查询用户信息
        List<UserEntity> userList = userRepository.findByIds(userIds);
        
        // 转换为Map
        return userList.stream()
                .collect(Collectors.toMap(
                        UserEntity::getId,
                        user -> user,
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未登录");
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

    private UserEntity createUserEntity(RegisterRequest request) {
        return UserEntity.builder()
                .username(request.getUsername())
                .password(SaSecureUtil.sha256(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getUsername())
                .phone(request.getPhone())
                .status(0)
                .build();
    }

    private boolean validatePassword(UserEntity user, String password) {
        return user.getPassword().equals(SaSecureUtil.sha256(password));
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