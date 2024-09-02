package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;

public interface IUserRepository {


    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    LoginFormVO findUserByUsername(String username);

    /**
     * 根据用户id查询用户
     * @param userId
     * @return
     */
    UserEntity findUserById(Long userId);

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @return
     */
    UserInfoEntity findUserInfoById(Long userId);

    /**
     * 根据页码和每页大小查询用户
     * @param page
     * @param size
     * @return
     */
    UserEntity findUserByPage(int page, int size);

    /**
     * 根据页码和每页大小查询管理员
     * @param page
     * @param size
     * @return
     */
    UserEntity findAdminByPage(int page, int size);
}
