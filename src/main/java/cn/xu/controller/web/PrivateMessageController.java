package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Conversation;
import cn.xu.model.entity.PrivateMessage;
import cn.xu.service.message.PrivateMessageService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 私信控制器
 * 提供私信发送、接收、会话管理等功能
 *
 * @author xu
 */
@Slf4j
@RestController
@RequestMapping("/api/message")
@Tag(name = "私信", description = "私信管理接口")
public class PrivateMessageController {

    @Resource
    private PrivateMessageService privateMessageService;

    // ==================== 会话相关 ====================

    @GetMapping("/conversations")
    @SaCheckLogin
    @Operation(summary = "获取会话列表")
    @ApiOperationLog(description = "获取会话列表")
    public ResponseEntity<List<Conversation>> getConversations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Conversation> conversations = privateMessageService.getConversationList(userId, page, size);
            
            return ResponseEntity.<List<Conversation>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(conversations)
                    .build();
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取会话列表失败");
        }
    }

    // ==================== 消息相关 ====================

    @GetMapping("/messages/{otherUserId}")
    @SaCheckLogin
    @Operation(summary = "获取与某用户的消息列表")
    @ApiOperationLog(description = "获取与某用户的消息列表")
    public ResponseEntity<List<PrivateMessage>> getMessages(
            @PathVariable Long otherUserId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "50") int size) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<PrivateMessage> messages = privateMessageService.getMessagesBetweenUsers(userId, otherUserId, page, size);
            
            return ResponseEntity.<List<PrivateMessage>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(messages)
                    .build();
        } catch (Exception e) {
            log.error("获取消息列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取消息列表失败");
        }
    }

    @PostMapping("/send")
    @SaCheckLogin
    @Operation(summary = "发送私信")
    @ApiOperationLog(description = "发送私信")
    public ResponseEntity<Void> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            Long senderId = StpUtil.getLoginIdAsLong();
            
            if (request.getReceiverId() == null) {
                throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "接收者ID不能为空");
            }
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "消息内容不能为空");
            }
            if (senderId.equals(request.getReceiverId())) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "不能给自己发送私信");
            }
            
            PrivateMessageService.SendResult result = privateMessageService.sendMessage(
                    senderId, request.getReceiverId(), request.getContent());
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(result.getMessage())
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送私信失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "发送私信失败");
        }
    }

    @PutMapping("/read/{senderId}")
    @SaCheckLogin
    @Operation(summary = "标记某用户消息已读")
    @ApiOperationLog(description = "标记某用户消息已读")
    public ResponseEntity<Void> markAsRead(@PathVariable Long senderId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            privateMessageService.markAsRead(userId, senderId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("标记已读成功")
                    .build();
        } catch (Exception e) {
            log.error("标记已读失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "标记已读失败");
        }
    }

    @GetMapping("/unread/count/{senderId}")
    @SaCheckLogin
    @Operation(summary = "获取某用户未读消息数量")
    @ApiOperationLog(description = "获取某用户未读消息数量")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long senderId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            long count = privateMessageService.getUnreadCount(userId, senderId);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(count)
                    .build();
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(0L)
                    .build();
        }
    }

    // ==================== 请求DTO ====================

    @Data
    public static class SendMessageRequest {
        private Long receiverId;
        private String content;
    }
}
