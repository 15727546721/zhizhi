package cn.xu.infrastructure.persistent.repository;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.xu.api.controller.web.user.RegisterRequest;
import cn.xu.domain.user.constant.UserStatus;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserPasswordEntity;
import cn.xu.domain.user.model.entity.UserRoleEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.common.ResponseCode;
import cn.xu.infrastructure.persistent.dao.IUserDao;
import cn.xu.infrastructure.persistent.dao.IUserRoleDao;
import cn.xu.infrastructure.persistent.po.User;
import cn.xu.infrastructure.persistent.po.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户仓储服务
 */
@Slf4j
@Repository
public class UserRepository implements IUserRepository {

    @Resource
    private IUserDao userDao;

    @Resource
    private IUserRoleDao userRoleDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public LoginFormVO findUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), ResponseCode.NULL_PARAMETER.getMessage());
        }
        return userDao.selectUserByUserName(username);
    }

    @Override
    public UserEntity findUserById(Long userId) {
        return convertToUserEntity(userDao.selectUserById(userId));
    }

    @Override
    public UserInfoEntity findUserInfoById(Long userId) {
        return userDao.selectUserInfoById(userId);
    }

    @Override
    public List<UserEntity> findUserByPage(int page, int size) {
        List<User> userList = userDao.selectUserByPage((page - 1) * size, size);
        log.info("findUserByPage: " + userList);

        return userList.stream()
                .map(this::convertToUserEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void saveUser(UserRoleEntity userRoleEntity) {
        User user = User.builder()
                .username(userRoleEntity.getUsername())
                .password(userRoleEntity.getPassword())
                .email(userRoleEntity.getEmail())
                .status(userRoleEntity.getStatus())
                .nickname(userRoleEntity.getNickname())
                .avatar(userRoleEntity.getAvatar())
                .build();
        transactionTemplate.execute(status -> {
            try {
                Long userId = userDao.insertUser(user);
                userRoleDao.insert(UserRole.builder()
                        .userId(userId)
                        .roleId(userRoleEntity.getRoleId())
                        .build());
                return null;
            } catch (Exception e) {
                log.error("添加用户失败: {}", userRoleEntity, e);
                status.setRollbackOnly();
                throw new AppException(ResponseCode.UN_ERROR.getCode(), "添加用户失败");
            }
        });
    }

    @Override
    public int updateUser(UserEntity userEntity) {
        User user = User.builder()
                .username(userEntity.getUsername())
                .password(SaSecureUtil.sha256(userEntity.getPassword()))
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
                .nickname(userEntity.getNickname())
                .avatar(userEntity.getAvatar())
                .build();
        return userDao.updateUser(user);
    }

    @Override
    public void deleteUser(Long userId) {
        transactionTemplate.execute(status -> {
            try {
                userRoleDao.deleteUserRoleByUserId(userId);
                userDao.deleteUser(userId);
                return null;
            } catch (Exception e) {
                log.error("删除用户失败, 用户ID: " + userId, e);
                status.setRollbackOnly();
                throw new AppException(ResponseCode.UN_ERROR.getCode(), "删除用户失败");
            }
        });
    }

    @Override
    public void updatePassword(UserPasswordEntity userPasswordEntity) {
        userDao.updatePassword(userPasswordEntity);
    }

    @Override
    public UserInfoEntity findUserInfoByUserId(Long id) {
        return userDao.selectUserInfoByUserId(id);
    }

    @Override
    public void updateUserInfo(UserInfoEntity userInfoEntity) {
        userDao.updateUserInfo(userInfoEntity);
    }

    @Override
    public void updateAvatar(Long id, String avatar) {
        userDao.updateAvatar(id, avatar);
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        User user = User.builder()
                .nickname(registerRequest.getNickname())
                .password(SaSecureUtil.sha256(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .status(UserStatus.NORMAL.getCode())
                .build();
        userDao.register(user);
    }

    @Override
    public UserEntity findUserLoginByEmailAndPassword(String email, String password) {
        return userDao.findUserLoginByEmailAndPassword(email, password);
    }

    @Override
    public Map<Long, UserEntity> findUserByIds(Set<Long> userIds) {
        return userDao.findUserByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
    }

    private UserEntity convertToUserEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
