package cn.xu.domain.follow.model.valueobject;

import lombok.Getter;

@Getter
public enum FollowStatus {
    UNFOLLOWED(0, "取消关注"),
    FOLLOWED(1, "已关注");

    private final Integer value;
    private final String desc;

    FollowStatus(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static FollowStatus valueOf(Integer value) {
        for (FollowStatus status : FollowStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}