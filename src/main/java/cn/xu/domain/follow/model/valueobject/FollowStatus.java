package cn.xu.domain.follow.model.valueobject;

public enum FollowStatus {
    UNFOLLOWED(0, "取消关注"),
    FOLLOWED(1, "已关注");

    private final Integer code;
    private final String desc;

    FollowStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FollowStatus of(Integer code) {
        for (FollowStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return UNFOLLOWED;
    }
} 