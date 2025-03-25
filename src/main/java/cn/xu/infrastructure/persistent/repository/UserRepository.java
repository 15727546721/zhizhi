package cn.xu.infrastructure.persistent.repository;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.model.valueobject.Email;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.dao.IRoleDao;
import cn.xu.infrastructure.persistent.dao.IUserDao;
import cn.xu.infrastructure.persistent.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserRepository implements IUserRepository {

    @Resource
    private IUserDao userDao;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private IRoleDao roleDao;

    @Override
    public UserEntity save(UserEntity user) {
        if (user.getId() == null) {
            userDao.insert(user);
        } else {
            userDao.update(user);
        }
        return user;
    }

    @Override
    public UserEntity findById(Long id) {
        return convertToUserEntity(userDao.selectById(id));
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return Optional.ofNullable(convertToUserEntity(userDao.selectByUsername(username)));
    }

    @Override
    public Optional<UserEntity> findByEmail(Email email) {
        return Optional.ofNullable(convertToUserEntity(userDao.selectByEmail(email.getValue())));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userDao.countByUsername(username) > 0;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userDao.countByEmail(email.getValue()) > 0;
    }

    @Override
    public List<UserEntity> findAll(Integer page, Integer size) {
        int offset = (page - 1) * size;
        return userDao.selectByPage(offset, size).stream()
                .map(this::convertToUserEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        transactionTemplate.execute(status -> {
            try {
                userDao.deleteById(id);
                return null;
            } catch (Exception e) {
                log.error("删除用户失败, 用户ID: " + id, e);
                status.setRollbackOnly();
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除用户失败");
            }
        });
    }

    @Override
    public List<UserEntity> findByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 调用DAO层查询
        List<User> users = userDao.findByIds(userIds);
        
        // 转换为领域实体
        return users.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserInfoEntity findUserInfoById(Long userId) {
        User user = userDao.selectById(userId);
        if (user == null) {
            return null;
        }
        return UserInfoEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .phone(user.getPhone())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .description(user.getDescription())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    @Override
    public LoginFormVO findUserByUsername(String username) {
        User user = userDao.selectByUsername(username);
        if (user == null) {
            return null;
        }
        return new LoginFormVO(user.getUsername(), user.getPassword());
    }

    @Override
    public List<String> findRolesByUserId(Long userId) {
        return roleDao.selectRolesByUserid(userId);
    }

    @Override
    public String getNicknameById(Long userId) {
        return userDao.selectById(userId).getNickname();
    }

    @Override
    public void update(UserEntity userEntity) {
        userDao.update(userEntity);
    }

    private UserEntity convertToUserEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .phone(user.getPhone())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .status(user.getStatus())
                .description(user.getDescription())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    private User convertToUserPO(UserEntity user) {
        return User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .phone(user.getPhone())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .status(user.getStatus())
                .description(user.getDescription())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    /**
     * PO转换为领域实体
     */
    private UserEntity convertToEntity(User user) {
        if (user == null) {
            return null;
        }
        return UserEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}