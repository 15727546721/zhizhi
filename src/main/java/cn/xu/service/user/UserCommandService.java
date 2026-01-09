package cn.xu.service.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.constants.FilePathConstants;
import cn.xu.integration.file.service.FileStorageService;
import cn.xu.model.dto.user.UpdateUserProfileRequest;
import cn.xu.model.entity.User;
import cn.xu.repository.UserRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * 用户命令服务
 * <p>负责用户资料更新、状态管理等写操作</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final UserQueryService queryService;

    /**
     * 更新用户资料
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateUserProfileRequest request) {
        if (userId == null || request == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }

        try {
            User user = queryService.getById(userId);
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
            String fileName = FilePathConstants.buildPath(file.getOriginalFilename());
            String storedFileName = fileStorageService.uploadFile(file, fileName);
            String avatarUrl = fileStorageService.getFileUrl(storedFileName);
            log.info("上传用户头像成功, avatarUrl: {}", avatarUrl);
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
    public void updateAvatar(Long userId, String avatarUrl) {
        if (userId == null || StringUtils.isBlank(avatarUrl)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }

        try {
            User user = queryService.getById(userId);
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            log.info("更新用户头像成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户头像失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新头像失败，请稍后重试");
        }
    }

    /**
     * 禁止用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void banUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }

        try {
            User user = queryService.getById(userId);
            user.setStatus(User.STATUS_DISABLED);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
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
            User user = queryService.getById(userId);
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

    /**
     * 删除用户（软删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户ID不能为空");
        }

        try {
            User user = queryService.getById(userId);
            user.setStatus(User.STATUS_DELETED);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            StpUtil.kickout(userId);
            log.info("删除用户成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除用户失败，请稍后重试");
        }
    }

    /**
     * 注销账户（需验证密码）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccount(Long userId, String password) {
        if (userId == null || StringUtils.isBlank(password)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }

        try {
            User user = queryService.getById(userId);
            if (!user.verifyPassword(password)) {
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(), "密码错误");
            }

            user.setStatus(User.STATUS_DELETED);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            StpUtil.kickout(userId);
            log.info("用户注销账户成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注销账户失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "注销账户失败，请稍后重试");
        }
    }
}
