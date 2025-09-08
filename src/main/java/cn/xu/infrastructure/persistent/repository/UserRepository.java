package cn.xu.infrastructure.persistent.repository;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.model.valobj.Username;
import cn.xu.domain.user.model.vo.LoginFormVO;
import cn.xu.domain.user.model.vo.UserFormVO;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import cn.xu.infrastructure.persistent.converter.UserConverter;
import cn.xu.infrastructure.persistent.converter.RoleConverter;
import cn.xu.infrastructure.persistent.dao.RoleMapper;
import cn.xu.infrastructure.persistent.dao.UserMapper;
import cn.xu.infrastructure.persistent.po.Role;
import cn.xu.infrastructure.persistent.po.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository implements IUserRepository {

    private final UserMapper userDao;
    private final TransactionTemplate transactionTemplate;
    private final RoleMapper roleDao;
    private final UserConverter userConverter;
    private final RoleConverter roleConverter;

    @Override
    public UserEntity save(UserEntity user) {
        User userPO = userConverter.toDataObject(user);
        if (userPO.getId() == null) {
            userDao.insert(userPO);
            user.setId(userPO.getId());
        } else {
            userDao.update(userPO);
        }
        return user;
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        User user = userDao.selectById(id);
        return Optional.ofNullable(userConverter.toDomainEntity(user));
    }

    @Override
    public Optional<UserEntity> findByUsername(Username username) {
        return Optional.ofNullable(userConverter.toDomainEntity(userDao.selectByUsername(username.getValue())));
    }

    @Override
    public Optional<UserEntity> findByEmail(Email email) {
        return Optional.ofNullable(userConverter.toDomainEntity(userDao.selectByEmail(email.getValue())));
    }

    @Override
    public boolean existsByUsername(Username username) {
        return userDao.countByUsername(username.getValue()) > 0;
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userDao.countByEmail(email.getValue()) > 0;
    }

    @Override
    public List<UserEntity> findByPage(Integer page, Integer size) {
        int offset = (page - 1) * size;
        return userDao.selectByPage(offset, size).stream()
                .map(userConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserEntity> findAll() {
        List<User> userEntityList = userDao.selectAll();
        return userEntityList.stream()
                .map(userConverter::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        transactionTemplate.execute(status -> {
            try {
                userDao.deleteById(id);
                return null;
            } catch (Exception e) {
                log.error("Delete user failed, user ID: " + id, e);
                status.setRollbackOnly();
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "Delete user failed");
            }
        });
    }

    @Override
    public List<UserEntity> findByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Call DAO layer query
        List<User> users = userDao.findByIds(userIds);
        
        // Convert to domain entities
        return users.stream()
                .map(userConverter::toDomainEntity)
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
    public void update(UserEntity userEntity) {
        User userPO = userConverter.toDataObject(userEntity);
        userDao.update(userPO);
    }
    
    /**
     * 更新用户的关注数
     * 
     * @param userId 用户ID
     * @param followCount 关注数
     */
    public void updateFollowCount(Long userId, Long followCount) {
        userDao.updateUserFollowCount(userId, followCount);
    }
    
    /**
     * 更新用户的粉丝数
     * 
     * @param userId 用户ID
     * @param fansCount 粉丝数
     */
    public void updateFansCount(Long userId, Long fansCount) {
        userDao.updateUserFansCount(userId, fansCount);
    }
    
    @Override
    public UserFormVO findUsernameAndPasswordByUsername(String username) {
        return userDao.selectUsernameAndPasswordByUsername(username);
    }
    
    @Override
    public List<String> findRolesByUserId(Long userId) {
        List<Role> roles = roleDao.findRolesByUserId(userId);
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }
}