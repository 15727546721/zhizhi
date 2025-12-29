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
 * 私信控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "私信", description = "私信管理接口")
public class PrivateMessageController {

    private final PrivateMessageService messageService;

    // ==================== 会话相关 ====================

    @GetMapping("/conversations")
    @SaCheckLogin
    @Operation(summary = "获取会话列表")
    @ApiOperationLog(description = "获取会话列表")
    public ResponseEntity<List<ConversationListVO>> getConversations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        Long userId = LoginUserUtil.getLoginUserId();
        log.info("[API] 获取会话列表 userId:{} page:{} size:{}", userId, page, size);
        
        List<ConversationListVO> list = messageService.getConversationList(userId, page, size);
        log.info("[API] 返回 {} 条会话", list.size());
        
        return ResponseEntity.<List<ConversationListVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(list)
                .build();
    }

    @PostMapping("/conversations/{targetUserId}")
    @SaCheckLogin
    @Operation(summary = "获取或创建对话")
    @ApiOperationLog(description = "获取或创建对话")
    public ResponseEntity<ConversationListVO> getOrCreateConversation(@PathVariable Long targetUserId) {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        log.info("[API] 获取或创建对话 currentUser:{} targetUser:{}", currentUserId, targetUserId);
        
        ConversationListVO conversation = messageService.getOrCreateConversation(currentUserId, targetUserId);
        return ResponseEntity.<ConversationListVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(conversation)
                .build();
    }

    @PostMapping("/conversations/{userId}/delete")
    @SaCheckLogin
    @Operation(summary = "删除会话")
    @ApiOperationLog(description = "删除会话")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long userId) {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        messageService.deleteConversation(currentUserId, userId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    // ==================== 消息相关 ====================

    @GetMapping("/conversations/{userId}/messages")
    @SaCheckLogin
    @Operation(summary = "获取消息列表")
    @ApiOperationLog(description = "获取消息列表")
    public ResponseEntity<List<PrivateMessage>> getMessages(
            @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "50") int size) {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        log.info("[API] 获取消息列表 currentUser:{} otherUser:{} page:{}", currentUserId, userId, page);
        
        List<PrivateMessage> messages = messageService.getMessages(currentUserId, userId, page, size);
        return ResponseEntity.<List<PrivateMessage>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(messages)
                .build();
    }

    @PostMapping
    @SaCheckLogin
    @Operation(summary = "发送私信")
    @ApiOperationLog(description = "发送私信")
    public ResponseEntity<SendResultVO> sendMessage(@RequestBody SendMessageRequest request) {
        Long senderId = LoginUserUtil.getLoginUserId();
        log.info("[API] 发送私信 sender:{} receiver:{}", senderId, request.getReceiverId());
        
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
        
        log.info("[API] 发送成功 messageId:{}", result.getMessageId());
        return ResponseEntity.<SendResultVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(vo)
                .info("发送成功")
                .build();
    }

    @PostMapping("/image")
    @SaCheckLogin
    @Operation(summary = "发送图片")
    @ApiOperationLog(description = "发送图片")
    public ResponseEntity<SendResultVO> sendImage(@RequestBody SendImageRequest request) {
        Long senderId = LoginUserUtil.getLoginUserId();
        
        if (request.getReceiverId() == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "接收者ID不能为空");
        }
        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "图片URL不能为空");
        }
        
        // 构造图片消息JSON
        String content = String.format("{\"type\":\"image\",\"url\":\"%s\"}", request.getImageUrl());
        PrivateMessageService.SendResult result = messageService.sendMessage(
                senderId, request.getReceiverId(), content);
        
        SendResultVO vo = new SendResultVO();
        vo.setMessageId(result.getMessageId());
        vo.setStatus(result.getStatus());
        
        return ResponseEntity.<SendResultVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(vo)
                .info("发送成功")
                .build();
    }

    @PostMapping("/conversations/{userId}/read")
    @SaCheckLogin
    @Operation(summary = "标记已读")
    @ApiOperationLog(description = "标记已读")
    public ResponseEntity<Void> markAsRead(@PathVariable Long userId) {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        messageService.markAsRead(currentUserId, userId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("标记成功")
                .build();
    }

    @PostMapping("/{messageId}/delete")
    @SaCheckLogin
    @Operation(summary = "删除消息")
    @ApiOperationLog(description = "删除消息")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        messageService.deleteMessage(currentUserId, messageId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    // ==================== 统计相关 ====================

    @GetMapping("/unread-count")
    @SaCheckLogin
    @Operation(summary = "获取总未读数")
    @ApiOperationLog(description = "获取总未读数")
    public ResponseEntity<Integer> getTotalUnreadCount() {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        int count = messageService.getTotalUnreadCount(currentUserId);
        return ResponseEntity.<Integer>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }

    @GetMapping("/conversations/{userId}/unread-count")
    @SaCheckLogin
    @Operation(summary = "获取与某用户的未读数")
    @ApiOperationLog(description = "获取与某用户的未读数")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable Long userId) {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        int count = messageService.getUnreadCount(currentUserId, userId);
        return ResponseEntity.<Integer>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .build();
    }

    // ==================== 权限检查 ====================

    @GetMapping("/can-send/{targetUserId}")
    @SaCheckLogin
    @Operation(summary = "检查私信权限")
    @ApiOperationLog(description = "检查私信权限")
    public ResponseEntity<PermissionCheckVO> canSendMessage(@PathVariable Long targetUserId) {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        log.info("[API] 检查私信权限 currentUser:{} targetUser:{}", currentUserId, targetUserId);
        
        PermissionCheckVO vo = new PermissionCheckVO();
        
        if (currentUserId.equals(targetUserId)) {
            vo.setCanSend(false);
            vo.setIsGreeting(false);
            vo.setReason("不能给自己发送私信");
        } else {
            PrivateMessageService.PermissionResult result = messageService.canSendDM(currentUserId, targetUserId);
            vo.setCanSend(result.isAllowed());
            vo.setIsGreeting(result.isGreeting());
            vo.setReason(result.isAllowed() ? null : result.getReason());
        }
        
        return ResponseEntity.<PermissionCheckVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(vo)
                .build();
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
