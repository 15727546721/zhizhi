package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.request.notification.NotificationRequest;
import cn.xu.api.web.model.vo.notification.NotificationVO;
import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.service.INotificationService;
import cn.xu.infrastructure.common.response.PageResponse;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知相关接口")
public class NotificationController {

    private final INotificationService notificationService;

    @GetMapping
    @Operation(summary = "获取通知列表")
    public ResponseEntity<PageResponse<List<NotificationVO>>> getUserNotifications(
            @Validated @Parameter(description = "查询参数") NotificationRequest request) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            List<NotificationAggregate> notifications = notificationService.getUserNotifications(
                    currentUserId, request.getType(), request.getPageNo(), request.getPageSize());
            
            List<NotificationVO> voList = notifications.stream()
                    .map(NotificationVO::fromAggregate)
                    .collect(Collectors.toList());

            long total = notificationService.getUnreadCount(currentUserId);
            return ResponseEntity.<PageResponse<List<NotificationVO>>>builder()
                    .code(20000)
                    .info("获取通知列表成功")
                    .data(PageResponse.of(request.getPageNo(), request.getPageSize(), total, voList))
                    .build();
        } catch (Exception e) {
            log.error("获取用户通知列表失败", e);
            return ResponseEntity.<PageResponse<List<NotificationVO>>>builder()
                    .code(20001)
                    .info("获取通知列表失败：" + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知数量")
    public ResponseEntity<Long> getUnreadCount() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            long count = notificationService.getUnreadCount(currentUserId);
            return ResponseEntity.<Long>builder()
                    .code(20000)
                    .info("获取未读通知数量成功")
                    .data(count)
                    .build();
        } catch (Exception e) {
            log.error("获取未读通知数量失败", e);
            return ResponseEntity.<Long>builder()
                    .code(20001)
                    .info("获取未读通知数量失败：" + e.getMessage())
                    .build();
        }
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记通知为已读")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.<Void>builder()
                    .code(20000)
                    .info("标记通知已读成功")
                    .build();
        } catch (Exception e) {
            log.error("标记通知已读失败: id={}", notificationId, e);
            return ResponseEntity.<Void>builder()
                    .code(20001)
                    .info("标记通知已读失败：" + e.getMessage())
                    .build();
        }
    }

    @PutMapping("/read/all")
    @Operation(summary = "标记所有通知为已读")
    public ResponseEntity<Void> markAllAsRead() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            notificationService.markAllAsRead(currentUserId);
            return ResponseEntity.<Void>builder()
                    .code(20000)
                    .info("标记所有通知已读成功")
                    .build();
        } catch (Exception e) {
            log.error("标记所有通知已读失败", e);
            return ResponseEntity.<Void>builder()
                    .code(20001)
                    .info("标记所有通知已读失败：" + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "通知ID") @PathVariable Long notificationId) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.<Void>builder()
                    .code(20000)
                    .info("删除通知成功")
                    .build();
        } catch (Exception e) {
            log.error("删除通知失败: id={}", notificationId, e);
            return ResponseEntity.<Void>builder()
                    .code(20001)
                    .info("删除通知失败：" + e.getMessage())
                    .build();
        }
    }
} 