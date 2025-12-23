package cn.xu.model.dto.notification;

import cn.xu.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知查询请求
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知查询请求参数")
public class NotificationRequest extends PageRequest {

    @Schema(description = "通知类型，支持逗号分隔多类型，如'1,2'。类型：0-系统 1-点赞 2-收藏 3-评论 4-回复 5-关注 6-@提及")
    private String type;

    @Schema(description = "是否仅查询未读通知，默认值为 false")
    private Boolean unreadOnly = false;
}