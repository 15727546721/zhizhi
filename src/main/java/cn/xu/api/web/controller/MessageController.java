package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.model.entity.MessageType;
import cn.xu.domain.message.service.IMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "消息接口", description = "用户消息相关接口")
public class MessageController {

    private final IMessageService messageService;

    @GetMapping
    @Operation(summary = "获取用户消息")
    @ApiOperationLog(description = "获取用户消息")
    public ResponseEntity<List<MessageEntity>> getUserMessages(
            @Parameter(description = "消息类型") @RequestParam(required = false) MessageType type,
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认为20") @RequestParam(defaultValue = "20") int size) {
        try {
            // 从SecurityContext获取当前用户ID
            Long currentUserId = StpUtil.getLoginIdAsLong();
            List<MessageEntity> messages = messageService.getUserMessages(currentUserId, type, page, size);
            return ResponseEntity.success(messages);
        } catch (Exception e) {
            log.error("获取用户消息失败", e);
            return ResponseEntity.error("获取用户消息失败");
        }
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数量")
    @ApiOperationLog(description = "获取未读消息数量")
    public ResponseEntity<Long> getUnreadCount() {
        try {
            // 从SecurityContext获取当前用户ID
            Long currentUserId = StpUtil.getLoginIdAsLong();
            long count = messageService.getUnreadCount(currentUserId);
            return ResponseEntity.success(count);
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            return ResponseEntity.error("获取未读消息数量失败");
        }
    }

    @PutMapping("/{messageId}/read")
    @Operation(summary = "标记消息为已读")
    @ApiOperationLog(description = "标记消息为已读")
    public ResponseEntity<Void> markAsRead(@Parameter(description = "消息ID") @PathVariable Long messageId) {
        try {
            messageService.markAsRead(messageId);
            return ResponseEntity.success();
        } catch (Exception e) {
            log.error("标记消息为已读失败", e);
            return ResponseEntity.error("标记消息为已读失败");
        }
    }

    @PutMapping("/read/all")
    @Operation(summary = "标记所有消息为已读")
    @ApiOperationLog(description = "标记所有消息为已读")
    public ResponseEntity<Void> markAllAsRead() {
        try {
            // 从SecurityContext获取当前用户ID
            Long currentUserId = StpUtil.getLoginIdAsLong();
            messageService.markAllAsRead(currentUserId);
            return ResponseEntity.success();
        } catch (Exception e) {
            log.error("标记所有消息为已读失败", e);
            return ResponseEntity.error("标记所有消息为已读失败");
        }
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "删除消息")
    @ApiOperationLog(description = "删除消息")
    public ResponseEntity<Void> deleteMessage(@Parameter(description = "消息ID") @PathVariable Long messageId) {
        try {
            // 从SecurityContext获取当前用户ID
            Long currentUserId = StpUtil.getLoginIdAsLong();
            messageService.deleteMessage(messageId, currentUserId);
            return ResponseEntity.success();
        } catch (Exception e) {
            log.error("删除消息失败", e);
            return ResponseEntity.error("删除消息失败");
        }
    }
}