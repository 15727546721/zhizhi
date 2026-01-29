package cn.xu.model.dto.message;

import lombok.Data;

/**
 * 更新用户私信设置请求
 */
@Data
public class UpdateUserMessageSettingsRequest {
    /**
     * 是否允许陌生人私信
     */
    private Boolean allowStrangerMessage;
}
