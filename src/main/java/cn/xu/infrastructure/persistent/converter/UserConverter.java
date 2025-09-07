package cn.xu.infrastructure.persistent.converter;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.model.valobj.Phone;
import cn.xu.domain.user.model.valobj.Username;
import cn.xu.infrastructure.persistent.po.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户领域实体转换器
 * 符合DDD架构的防腐层模式
 */
@Component
public class UserConverter {

    /**
     * 将领域实体转换为持久化对象
     *
     * @param entity 用户领域实体
     * @return 用户持久化对象
     */
    public User toDataObject(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return User.builder()
                .id(entity.getId())
                .username(entity.getUsernameValue())
                .email(entity.getEmailValue())
                .nickname(entity.getNickname())
                .avatar(entity.getAvatar())
                .gender(entity.getGender())
                .phone(entity.getPhoneValue())
                .region(entity.getRegion())
                .birthday(entity.getBirthday())
                .status(entity.getStatusCode())
                .description(entity.getDescription())
                .followCount(entity.getFollowCount() != null ? entity.getFollowCount() : 0)
                .fansCount(entity.getFansCount() != null ? entity.getFansCount() : 0)
                .likeCount(entity.getLikeCount() != null ? entity.getLikeCount() : 0)
                .lastLoginTime(entity.getLastLoginTime())
                .lastLoginIp(entity.getLastLoginIp())
                .createTime(entity.getCreateTime() != null ? entity.getCreateTime() : LocalDateTime.now())
                .updateTime(entity.getUpdateTime() != null ? entity.getUpdateTime() : LocalDateTime.now())
                .build();
    }

    /**
     * 将持久化对象转换为领域实体
     *
     * @param dataObject 用户持久化对象
     * @return 用户领域实体
     */
    public UserEntity toDomainEntity(User dataObject) {
        if (dataObject == null) {
            return null;
        }
        
        return UserEntity.builder()
                .id(dataObject.getId())
                .username(dataObject.getUsername() != null ? new Username(dataObject.getUsername()) : null)
                .email(dataObject.getEmail() != null ? new Email(dataObject.getEmail()) : null)
                .nickname(dataObject.getNickname())
                .avatar(dataObject.getAvatar())
                .gender(dataObject.getGender())
                .phone(dataObject.getPhone() != null ? new Phone(dataObject.getPhone()) : null)
                .region(dataObject.getRegion())
                .birthday(dataObject.getBirthday())
                .status(UserEntity.UserStatus.fromCode(dataObject.getStatus()))
                .description(dataObject.getDescription())
                .followCount(dataObject.getFollowCount() != null ? dataObject.getFollowCount() : 0)
                .fansCount(dataObject.getFansCount() != null ? dataObject.getFansCount() : 0)
                .likeCount(dataObject.getLikeCount() != null ? dataObject.getLikeCount() : 0)
                .lastLoginTime(dataObject.getLastLoginTime())
                .lastLoginIp(dataObject.getLastLoginIp())
                .createTime(dataObject.getCreateTime())
                .updateTime(dataObject.getUpdateTime())
                .build();
    }

    /**
     * 将持久化对象列表转换为领域实体列表
     *
     * @param dataObjects 持久化对象列表
     * @return 领域实体列表
     */
    public List<UserEntity> toDomainEntities(List<User> dataObjects) {
        if (dataObjects == null || dataObjects.isEmpty()) {
            return new ArrayList<>();
        }
        
        return dataObjects.stream()
                .map(this::toDomainEntity)
                .collect(Collectors.toList());
    }

    /**
     * 将领域实体列表转换为持久化对象列表
     *
     * @param entities 领域实体列表
     * @return 持久化对象列表
     */
    public List<User> toDataObjects(List<UserEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        
        return entities.stream()
                .map(this::toDataObject)
                .collect(Collectors.toList());
    }

    /**
     * 更新持久化对象的部分字段（来自领域实体）
     *
     * @param target 目标持久化对象
     * @param source 源领域实体
     * @return 更新后的持久化对象
     */
    public User updateDataObject(User target, UserEntity source) {
        if (target == null || source == null) {
            return target;
        }

        if (source.getUsernameValue() != null) {
            target.setUsername(source.getUsernameValue());
        }
        
        if (source.getEmailValue() != null) {
            target.setEmail(source.getEmailValue());
        }
        
        if (source.getNickname() != null) {
            target.setNickname(source.getNickname());
        }
        
        if (source.getAvatar() != null) {
            target.setAvatar(source.getAvatar());
        }
        
        if (source.getGender() != null) {
            target.setGender(source.getGender());
        }
        
        if (source.getPhoneValue() != null) {
            target.setPhone(source.getPhoneValue());
        }
        
        if (source.getRegion() != null) {
            target.setRegion(source.getRegion());
        }
        
        if (source.getBirthday() != null) {
            target.setBirthday(source.getBirthday());
        }
        
        if (source.getStatusCode() != null) {
            target.setStatus(source.getStatusCode());
        }
        
        if (source.getDescription() != null) {
            target.setDescription(source.getDescription());
        }
        
        if (source.getFollowCount() != null) {
            target.setFollowCount(source.getFollowCount());
        }
        
        if (source.getFansCount() != null) {
            target.setFansCount(source.getFansCount());
        }
        
        if (source.getLikeCount() != null) {
            target.setLikeCount(source.getLikeCount());
        }
        
        if (source.getLastLoginTime() != null) {
            target.setLastLoginTime(source.getLastLoginTime());
        }
        
        if (source.getLastLoginIp() != null) {
            target.setLastLoginIp(source.getLastLoginIp());
        }

        // 始终将更新时间设置为当前时间
        target.setUpdateTime(LocalDateTime.now());

        return target;
    }
}