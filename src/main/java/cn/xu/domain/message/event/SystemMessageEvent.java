package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;

public class SystemMessageEvent extends BaseMessageEvent {

    public SystemMessageEvent(String title, String content, Long receiverId) {
        super(null, receiverId, content, null, MessageType.SYSTEM);
        setTitle(title);
    }

    @Override
    public void validate() {
        if (getTitle() == null || getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("系统消息标题不能为空");
        }
    }
}
