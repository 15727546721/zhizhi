package cn.xu.infrastructure.persistent.repository;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.infrastructure.persistent.dao.IUserDao;
import cn.xu.infrastructure.persistent.po.User;
import cn.xu.types.common.Constants;
import cn.xu.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 用户仓储服务
 */
@Slf4j
@Repository
public class UserRepository implements IUserRepository {

    @Resource
    private IUserDao userDao;

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
        UserInfoEntity userInfo= userDao.selectUserInfoById(userId);

        return userInfo;
    }

    @Override
    public List<UserEntity> findUserByPage(int page, int size) {
        List<UserEntity> userEntityList = userDao.selectUserByPage(page, size);
        log.info("findUserByPage: " + userEntityList);
        return userEntityList;
    }

    @Override
    public List<UserEntity> findAdminByPage(int page, int size) {
        List<UserEntity> userEntityList = userDao.selectAdminByPage(page, size);
        log.info("findAdminByPage: " + userEntityList);
        return userEntityList;
    }

    @Override
    public int saveUser(UserEntity userEntity) {
        User user = User.builder()
                .username(userEntity.getUsername())
                .password(SaSecureUtil.sha256(userEntity.getPassword()))
                .email(userEntity.getEmail())
                .status(userEntity.getStatus())
                .nickname(userEntity.getNickname())
                .avatar(null)
                .createdTime(new Date())
                .updatedTime(new Date())
                .build();
        int result = userDao.insertUser(user);
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
                .updatedTime(new Date()) // 更新更新时间
                .build();
        int result = userDao.updateUser(user);
        return result;
    }

    @Override
    public int deleteUser(Long userId) {
        int result = userDao.deleteUser(userId);
        return result;
    }
}
