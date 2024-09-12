package cn.xu.infrastructure.persistent.repository;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.infrastructure.persistent.dao.IUserDao;
import cn.xu.infrastructure.persistent.dao.IUserRoleDao;
import cn.xu.infrastructure.persistent.po.User;
import cn.xu.types.common.Constants;
import cn.xu.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.List;
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
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode()
                    , Constants.ResponseCode.NULL_PARAMETER.getInfo());
        }
        LoginFormVO loginFormVO = userDao.selectUserByUserName(username);
        return loginFormVO;
    }

    @Override
    public UserEntity findUserById(Long userId) {

        UserEntity userEntity = userDao.selectUserById(userId);
        return userEntity;
    }

    @Override
    public UserInfoEntity findUserInfoById(Long userId) {
        UserInfoEntity userInfo = userDao.selectUserInfoById(userId);

        return userInfo;
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
    public List<UserEntity> findAdminByPage(int page, int size) {
        List<User> userList = userDao.selectAdminByPage(page - 1, size);
        log.info("findAdminByPage: " + userList);

        return userList.stream()
                .map(this::convertToUserEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int saveUser(UserEntity userEntity) {
        User user = User.builder()
                .username(userEntity.getUsername())
                .password(SaSecureUtil.sha256(userEntity.getPassword()))
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
                .nickname(userEntity.getNickname())
                .avatar(null) // 头像暂时不用设置
                .build();
        int result;
        try {
            result = userDao.insertUser(user);
        } catch (DuplicateKeyException e) {
            log.error("saveUser: " + e.getMessage());
            throw new AppException(Constants.ResponseCode.DUPLICATE_KEY.getCode(),
                    Constants.ResponseCode.DUPLICATE_KEY.getInfo());
        }
        return result;
    }

    @Override
    public int updateUser(UserEntity userEntity) {
        User user = User.builder()
                .username(userEntity.getUsername())
                .password(SaSecureUtil.sha256(userEntity.getPassword()))
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
                .nickname(userEntity.getNickname())
                .avatar(userEntity.getAvatar()) // 如果有新的头像，设置它
                .build();
        int result = userDao.updateUser(user);
        return result;
    }

    @Override
    public void deleteUser(Long userId) {
        try {
            transactionTemplate.execute(status -> {
                try {
                    // 删除用户角色关联记录
                    userRoleDao.deleteUserRoleByUserId(userId);
                    // 删除用户记录
                    userDao.deleteUser(userId);
                    return null; // 没有异常则返回 null
                } catch (Exception e) {
                    // 记录错误日志
                    log.error("删除用户失败, 用户ID: " + userId, e);
                    // 标记事务为回滚
                    status.setRollbackOnly();
                    // 抛出异常以确保事务回滚
                    throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "删除用户失败");
                }
            });
        } catch (RuntimeException e) {
            // 处理事务外层的异常（可选）
            log.error("删除用户过程中发生异常", e);
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "出现未知异常，删除用户失败");
        }
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
