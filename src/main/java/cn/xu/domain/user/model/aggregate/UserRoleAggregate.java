package cn.xu.domain.user.model.aggregate;

import cn.xu.domain.user.model.entity.RoleEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class UserRoleAggregate {
    private UserEntity user;
    private List<RoleEntity> roles;

    public UserInfoEntity toUserInfoEntity() {
        UserInfoEntity userInfo = UserInfoEntity.builder()
                .id(user.getId())
                .username(user.getUsernameValue())
                .email(user.getEmailValue())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .phone(user.getPhoneValue())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .description(user.getDescription())
                .status(user.getStatusCode())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();

        List<String> roleNames = roles.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());
        userInfo.setRoles(roleNames);

        return userInfo;
    }
} 