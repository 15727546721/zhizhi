package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.PrivateMessage;
import cn.xu.model.vo.message.ConversationListVO;
import cn.xu.service.message.PrivateMessageService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信控制器（用户视角设计）
 *
 * <p>API路径：/api/messages
 *
 * <p>权限规则:
 * <ul>
 *   <li>互关 → 无限制发送</li>
 *   <li>对方回复过 → 无限制发送</li>
 *   <li>陌生人/单向关注 → 可发1条打招呼消息</li>
 *   <li>拉黑 → 不可发送</li>
 * </ul>
 *
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "私信", description = "私信管理接口")
public class PrivateMessageController {

    private final PrivateMessageService messageService;

    // ==================== 会话相关 ====================

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    @SaCheckLogin
    @Operation(summary = "获取会话列表")
    @ApiOperationLog(description = "获取会话列表")
    public ResponseEntity<List<ConversationListVO>> getConversations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            log.info("[会话列表] 当前用户ID: {}", userId);
            List<ConversationListVO> list = messageService.getConversationList(userId, page, size);
            // 打印返回的会话数据便于调试
            list.forEach(conv -> log.info("[会话列表] 会话: ownerId(当前用户)={}, otherUserId={}, otherName={}", 
                    userId, conv.getUserId(), conv.getUserName()));
            return ResponseEntity.<List<ConversationListVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(list)
                    .build();
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取会话列表失败");
        }
    }

    /**
     * 获取或创建对话
     */
    @PostMapping("/conversations/{targetUserId}")
    @SaCheckLogin
    @Operation(summary = "获取或创建对话")
    @ApiOperationLog(description = "获取或创建对话")
    public ResponseEntity<ConversationListVO> getOrCreateConversation(@PathVariable Long targetUserId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            ConversationListVO conversation = messageService.getOrCreateConversation(currentUserId, targetUserId);
            return ResponseEntity.<ConversationListVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(conversation)
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取或创建对话失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "操作失败");
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/conversations/{userId}")
    @SaCheckLogin
    @Operation(summary = "删除会话")
    @ApiOperationLog(description = "删除会话")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long userId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            messageService.deleteConversation(currentUserId, userId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (Exception e) {
            log.error("删除会话失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除失败");
        }
    }

    // ==================== 消息相关 ====================

    /**
     * 获取与某用户的消息列表
     */
    @GetMapping("/conversations/{userId}/messages")
    @SaCheckLogin
    @Operation(summary = "获取消息列表")
    @ApiOperationLog(description = "获取消息列表")
    public ResponseEntity<List<PrivateMessage>> getMessages(
            @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "50") int size) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            List<PrivateMessage> messages = messageService.getMessages(currentUserId, userId, page, size);
            return ResponseEntity.<List<PrivateMessage>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(messages)
                    .build();
        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取消息列表失败");
        }
    }

    /**
     * 发送私信
     */
    @PostMapping
    @SaCheckLogin
    @Operation(summary = "发送私信")
    @ApiOperationLog(description = "发送私信")
    public ResponseEntity<SendResultVO> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            Long senderId = LoginUserUtil.getLoginUserId();
            if (request.getReceiverId() == null) {
                throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "接收者ID不能为空");
            }
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "消息内容不能为空");
            }
            PrivateMessageService.SendResult result = messageService.sendMessage(
                    senderId, request.getReceiverId(), request.getContent());
            SendResultVO vo = new SendResultVO();
            vo.setMessageId(result.getMessageId());
            vo.setStatus(result.getStatus());
            return ResponseEntity.<SendResultVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(vo)
                    .info("发送成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送私信失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "发送失败");
        }
    }

    /**
     * 发送图片消息
     */
    @PostMapping("/image")
    @SaCheckLogin
    @Operation(summary = "发送图片")
    @ApiOperationLog(description = "发送图片")
    public ResponseEntity<SendResultVO> sendImage(@RequestBody SendImageRequest request) {
        try {
            Long senderId = LoginUserUtil.getLoginUserId();
            if (request.getReceiverId() == null) {
                throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "接收者ID不能为空");
            }
            if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
                throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "图片URL不能为空");
            }
            PrivateMessageService.SendResult result = messageService.sendImage(
                    senderId, request.getReceiverId(), request.getImageUrl());
            SendResultVO vo = new SendResultVO();
            vo.setMessageId(result.getMessageId());
            vo.setStatus(result.getStatus());
            return ResponseEntity.<SendResultVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(vo)
                    .info("发送成功")
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送图片失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "发送失败");
        }
    }

    /**
     * 标记消息已读
     */
    @PostMapping("/conversations/{userId}/read")
    @SaCheckLogin
    @Operation(summary = "标记已读")
    @ApiOperationLog(description = "标记已读")
    public ResponseEntity<Void> markAsRead(@PathVariable Long userId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            messageService.markAsRead(currentUserId, userId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("标记成功")
                    .build();
        } catch (Exception e) {
            log.error("标记已读失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "标记失败");
        }
    }

    // ==================== 消息管理 ====================

    /**
     * 删除消息（仅自己不可见）
     */
    @DeleteMapping("/{messageId}")
    @SaCheckLogin
    @Operation(summary = "删除消息")
    @ApiOperationLog(description = "删除消息")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            messageService.deleteMessage(currentUserId, messageId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (Exception e) {
            log.error("删除消息失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除失败");
        }
    }

    // ==================== 统计相关 ====================

    /**
     * 获取总未读消息数
     */
    @GetMapping("/unread-count")
    @SaCheckLogin
    @Operation(summary = "获取总未读数")
    @ApiOperationLog(description = "获取总未读数")
    public ResponseEntity<Integer> getTotalUnreadCount() {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            int count = messageService.getTotalUnreadCount(currentUserId);
            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(count)
                    .build();
        } catch (Exception e) {
            log.error("获取未读数失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取失败");
        }
    }

    /**
     * 获取与某用户的未读消息数
     */
    @GetMapping("/conversations/{userId}/unread-count")
    @SaCheckLogin
    @Operation(summary = "获取与某用户的未读数")
    @ApiOperationLog(description = "获取与某用户的未读数")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable Long userId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            int count = messageService.getUnreadCount(currentUserId, userId);
            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(count)
                    .build();
        } catch (Exception e) {
            log.error("获取未读数失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取失败");
        }
    }

    // ==================== 权限检查 ====================

    /**
     * 检查是否可以向目标用户发送私信
     */
    @GetMapping("/can-send/{targetUserId}")
    @SaCheckLogin
    @Operation(summary = "检查私信权限")
    @ApiOperationLog(description = "检查私信权限")
    public ResponseEntity<PermissionCheckVO> canSendMessage(@PathVariable Long targetUserId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserId();
            PermissionCheckVO vo = new PermissionCheckVO();
            
            // 不能给自己发私信
            if (currentUserId.equals(targetUserId)) {
                vo.setCanSend(false);
                vo.setIsGreeting(false);
                vo.setReason("不能给自己发送私信");
            } else {
                // 调用Service层完整权限校验
                PrivateMessageService.PermissionResult result = messageService.canSendDM(currentUserId, targetUserId);
                vo.setCanSend(result.isAllowed());
                vo.setIsGreeting(result.isGreeting());
                vo.setReason(result.isAllowed() ? null : result.getReason());
            }
            
            return ResponseEntity.<PermissionCheckVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(vo)
                    .build();
        } catch (Exception e) {
            log.error("检查私信权限失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "检查权限失败");
        }
    }

    // ==================== DTO ====================

    @Data
    public static class SendMessageRequest {
        private Long receiverId;
        private String content;
    }

    @Data
    public static class SendImageRequest {
        private Long receiverId;
        private String imageUrl;
    }

    @Data
    public static class SendResultVO {
        private Long messageId;
        private Integer status;
    }

    @Data
    public static class PermissionCheckVO {
        private Boolean canSend;
        private Boolean isGreeting;
        private String reason;
    }
}