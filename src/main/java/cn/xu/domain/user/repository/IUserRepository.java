package cn.xu.domain.user.repository;

import cn.xu.api.controller.web.user.RegisterRequest;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserPasswordEntity;
import cn.xu.domain.user.model.entity.UserRoleEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;

import java.util.List;

public interface IUserRepository {


    /**
     * 根据用户名查询用户
     *
     * @param username
     * @return
     */
    LoginFormVO findUserByUsername(String username);

    /**
     * 根据用户id查询用户
     *
     * @param userId
     * @return
     */
    UserEntity findUserById(Long userId);

    /**
     * 根据用户id查询用户信息
     *
     * @param userId
     * @return
     */
    UserInfoEntity findUserInfoById(Long userId);

    /**
     * 根据页码和每页大小查询用户
     *
     * @param page
     * @param size
     * @return
     */
    List<UserEntity> findUserByPage(int page, int size);

    /**
     * 保存用户
     *
     * @param userRoleEntity
     * @return
     */
    void saveUser(UserRoleEntity userRoleEntity);

    /**
     * 更新用户
     *
     * @param userEntity
     * @return
     */
    int updateUser(UserEntity userEntity);

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    void deleteUser(Long userId);

    void updatePassword(UserPasswordEntity userPasswordEntity);

    UserInfoEntity findUserInfoByUserId(Long id);

    void updateUserInfo(UserInfoEntity userInfoEntity);

    void updateAvatar(Long id, String avatar);

    void register(RegisterRequest registerRequest);

    UserEntity findUserLoginByEmailAndPassword(String email, String password);
}
