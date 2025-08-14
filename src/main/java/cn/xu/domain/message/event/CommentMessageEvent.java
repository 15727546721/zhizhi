package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;

public class CommentMessageEvent extends BaseMessageEvent {

    public CommentMessageEvent(Long senderId, Long receiverId, String content, Long articleId) {
        super(senderId, receiverId, content, articleId, MessageType.COMMENT);
    }

    @Override
    public void validate() {
        if (getContent() == null || getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("评论内容不能为空");
        }
    }
}
