package cn.xu.domain.user.service.user;

import cn.xu.domain.user.model.entity.UserEntity;
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

    @Override
    public List<UserEntity> queryUserList(int page, int size) {
        List<UserEntity> userEntityList = userRepository.findUserByPage(page, size);
        return userEntityList;
    }

    @Override
    public List<UserEntity> queryAdminList(int page, int size) {
        List<UserEntity> userEntityList = userRepository.findAdminByPage(page, size);
        return userEntityList;
    }
}
