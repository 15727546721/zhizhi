package cn.xu.event.events;

import cn.xu.event.core.BaseEvent;
import lombok.Getter;

/**
 * 分享事件
 */
@Getter
public class ShareEvent extends BaseEvent {

    /**
     * 帖子ID
     */
    private final Long postId;

    /**
     * 帖子作者ID
     */
    private final Long authorId;

    /**
     * 分享用户ID（可能为空）
     */
    private final Long sharerId;

    /**
     * 分享平台
     */
    private final String platform;

    public ShareEvent(Long postId, Long authorId, Long sharerId, String platform) {
        super(sharerId, EventAction.CREATE);
        this.postId = postId;
        this.authorId = authorId;
        this.sharerId = sharerId;
        this.platform = platform;
    }
}
