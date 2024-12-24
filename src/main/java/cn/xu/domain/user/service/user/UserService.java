package cn.xu.domain.user.service.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.user.UserPasswordRequest;
import cn.xu.domain.permission.repository.IPermissionRepository;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserPasswordEntity;
import cn.xu.domain.user.model.entity.UserRoleEntity;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class UserService implements IUserService {
    @Resource
    private IUserRepository userRepository;
    @Resource
    private IPermissionRepository permissionRepository;

    @Override
    public List<UserEntity> queryUserList(int page, int size) {
        List<UserEntity> userEntityList = userRepository.findUserByPage(page, size);
        log.info("query user list, page:{}, size:{}, result size:{}", page, size, userEntityList.size());
        return userEntityList;
    }

    @Override
    public void addUser(UserRoleEntity userRole) {
        userRepository.saveUser(userRole);
    }

    @Override
    public int updateUser(UserEntity userEntity) {
        int result = userRepository.updateUser(userEntity);
        return result;
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteUser(id);
    }

    @Override
    public void updatePassword(UserPasswordRequest userPasswordRequest) {
        userRepository.updatePassword(UserPasswordEntity.builder()
                .userId(StpUtil.getLoginIdAsLong())
                .oldPassword(userPasswordRequest.getOldPassword())
                .newPassword(userPasswordRequest.getNewPassword())
                .build());
    }

    @Override
    public UserInfoEntity queryUserInfo(Long id) {
        UserInfoEntity userInfoEntity = userRepository.findUserInfoByUserId(id);
        return userInfoEntity;
    }

    @Override
    public void updateUserInfo(UserInfoEntity userInfoEntity) {
        userRepository.updateUserInfo(userInfoEntity);
    }

    @Override
    public void uploadAvatar(Long id, String avatar) {
        userRepository.updateAvatar(id, avatar);
    }
}
