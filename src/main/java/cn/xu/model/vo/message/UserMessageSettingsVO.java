package cn.xu.model.vo.message;

import lombok.Data;

/**
 * 用户私信设置VO
 */
@Data
public class UserMessageSettingsVO {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否允许陌生人私信
     */
    private Boolean allowStrangerMessage;
}
