package cn.xu.model.vo.message;

import cn.xu.common.constants.BooleanConstants;
import cn.xu.model.enums.ConversationRelationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话列表VO
 * <p>该类用于返回当前用户的会话列表，并且JOIN用户信息</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationListVO {

    /** 会话ID */
    private Long id;

    /** 相关用户ID */
    private Long userId;

    /** 相关用户用户名 */
    private String userName;

    /** 相关用户头像 */
    private String userAvatar;

    /** 会话类型: 0-单向关注 1-互相关注 2-普通聊天 */
    private Integer relationType;

    /** 会话描述 */
    private String relationDesc;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 最近一条消息 */
    private String lastMessage;

    /** 最近一条消息时间 */
    private LocalDateTime lastMessageTime;

    /** 最近一条消息时间字符串 */
    private String lastMessageTimeStr;

    /** 最近一条消息是否是自己发送的 */
    private Boolean lastMessageIsMine;

    /** 是否置顶 */
    private Boolean isPinned;

    /** 是否静音 */
    private Boolean isMuted;

    /** 是否屏蔽 */
    private Boolean isBlocked;
    
    /** 是否是消息请求（陌生人发来的首条消息，未互关且未回复） */
    private Boolean isMessageRequest;

    /**
     * 从 Entity 转换成 VO
     */
    public static ConversationListVO fromEntity(cn.xu.model.entity.UserConversation entity) {
        if (entity == null) return null;

        ConversationRelationType relationType = ConversationRelationType.fromCode(entity.getRelationType());
        
        // 判断是否是消息请求：非互关 + 不是发起人 + 会话状态为待回复或陌生人关系
        boolean isMessageRequest = entity.getRelationType() != cn.xu.model.entity.UserConversation.RELATION_MUTUAL
                && !BooleanConstants.isTrue(entity.getIsInitiator())
                && (entity.getConversationStatus() == cn.xu.model.entity.UserConversation.STATUS_PENDING
                    || entity.getRelationType() == cn.xu.model.entity.UserConversation.RELATION_STRANGER);

        return ConversationListVO.builder()
                .id(entity.getId())
                .userId(entity.getOtherUserId())
                .userName(entity.getOtherNickname())
                .userAvatar(entity.getOtherAvatar())
                .relationType(entity.getRelationType())
                .relationDesc(relationType.getDescription())
                .unreadCount(entity.getUnreadCount() == null ? 0 : entity.getUnreadCount())
                .lastMessage(entity.getLastMessage())
                .lastMessageTime(entity.getLastMessageTime())
                .lastMessageIsMine(BooleanConstants.isTrue(entity.getLastMessageIsMine()))
                .isPinned(BooleanConstants.isTrue(entity.getIsPinned()))
                .isMuted(BooleanConstants.isTrue(entity.getIsMuted()))
                .isBlocked(BooleanConstants.isTrue(entity.getIsBlocked()))
                .isMessageRequest(isMessageRequest)
                .build();
    }
}
