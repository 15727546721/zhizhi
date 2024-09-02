package cn.xu.domain.user.service.user;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserService implements IUserService {
    @Resource
    private IUserRepository userRepository;

    @Override
    public UserEntity queryUserList(int page, int size) {
        UserEntity userEntity = userRepository.findUserByPage(page, size);
        return userEntity;
    }

    @Override
    public UserEntity queryAdminList(int page, int size) {
        UserEntity userEntity = userRepository.findAdminByPage(page, size);
        return userEntity;
    }
}
