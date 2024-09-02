package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.infrastructure.persistent.dao.IUserDao;
import cn.xu.types.common.Constants;
import cn.xu.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;


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
    public UserEntity findUserByPage(int page, int size) {
        UserEntity userEntity = userDao.selectUserByPage(page, size);
        return userEntity;
    }

    @Override
    public UserEntity findAdminByPage(int page, int size) {
        UserEntity userEntity = userDao.selectAdminByPage(page, size);
        return userEntity;
    }
}
