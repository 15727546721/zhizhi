package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;

public class LikeMessageEvent extends BaseMessageEvent {

    public LikeMessageEvent(Long senderId, Long receiverId, String content, Long targetId) {
        super(senderId, receiverId, content, targetId, MessageType.LIKE);
    }

    @Override
    public void validate() {
        if (getContent() == null || getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("点赞内容不能为空");
        }
    }
}
