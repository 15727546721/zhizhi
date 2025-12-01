package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.notification.NotificationRequest;
import cn.xu.model.entity.Notification;
import cn.xu.model.vo.notification.NotificationResponse;
import cn.xu.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知控制器
 * 
 * <p>提供通知列表查询、标记已读、删除通知等功能接口
 * 
 * @author xu
 * @since 2025-11-25
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "获取通知列表")
    @ApiOperationLog(description = "获取通知列表")
    public ResponseEntity<PageResponse<List<NotificationResponse>>> getUserNotifications(
            @Validated @Parameter(description = "查询参数") NotificationRequest request) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            List<Notification> notifications = notificationService.getUserNotifications(
                    currentUserId, request.getType(), request.getPageNo(), request.getPageSize());
            
            List<NotificationResponse> voList = notifications.stream()
                    .map(NotificationResponse::from)
                    .collect(Collectors.toList());

            long total = notificationService.getUnreadCount(currentUserId);
            return ResponseEntity.<PageResponse<List<NotificationResponse>>>builder()
                    .code(20000)
                    .info("获取通知列表成功")
                    .data(PageResponse.of(request.getPageNo(), request.getPageSize(), total, voList))
                    .build();
        } catch (Exception e) {
            log.error("获取用户通知列表失败", e);
            return ResponseEntity.<PageResponse<List<NotificationResponse>>>builder()
                    .code(20001)
                    .info("获取通知列表失败：" + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知数量")
    @ApiOperationLog(description = "获取未读通知数量")
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

//    @GetMapping("/unread/list/count")
//    @Operation(summary = "获取未读各个通知数量")
//    @ApiOperationLog(description = "获取未读各个通知数量")
//    public ResponseEntity<List<Long>> getUnreadListCount() {
//        try {
//            Long currentUserId = StpUtil.getLoginIdAsLong();
//            List<NotificationType> types = NotificationType.values();
//            List<Long> counts = types.stream()
//                    .map(type -> notificationService.getUnreadCount(currentUserId, type))
//                    .collect(Collectors.toList());
//            return ResponseEntity.<List<Long>>builder()
//                    .code(20000)
//                    .info("获取未读各个通知数量成功")
//                    .data(counts)
//                    .build();
//        } catch (Exception e) {
//            log.error("获取未读各个通知数量失败", e);
//            return ResponseEntity.<List<Long>>builder()
//                    .code(20001)
//                    .info("获取未读各个通知数量失败：" + e.getMessage())
//                    .build();
//        }
//    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "标记通知为已读")
    @ApiOperationLog(description = "标记通知为已读")
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
    @ApiOperationLog(description = "标记所有通知为已读")
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
    @ApiOperationLog(description = "删除通知")
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

    @GetMapping("/unread/count-by-type")
    @Operation(summary = "获取各类型未读通知数量")
    @ApiOperationLog(description = "获取各类型未读通知数量")
    public ResponseEntity<Map<Integer, Long>> getUnreadCountByType() {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            Map<Integer, Long> countMap = notificationService.getUnreadCountByType(currentUserId);
            return ResponseEntity.<Map<Integer, Long>>builder()
                    .code(20000)
                    .info("获取各类型未读通知数量成功")
                    .data(countMap)
                    .build();
        } catch (Exception e) {
            log.error("获取各类型未读通知数量失败", e);
            return ResponseEntity.<Map<Integer, Long>>builder()
                    .code(20001)
                    .info("获取各类型未读通知数量失败：" + e.getMessage())
                    .build();
        }
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除通知")
    @ApiOperationLog(description = "批量删除通知")
    public ResponseEntity<Void> batchDeleteNotifications(
            @RequestBody @Parameter(description = "通知ID列表") Map<String, List<Long>> request) {
        try {
            Long currentUserId = StpUtil.getLoginIdAsLong();
            List<Long> notificationIds = request.get("notificationIds");
            if (notificationIds == null || notificationIds.isEmpty()) {
                return ResponseEntity.<Void>builder()
                        .code(20001)
                        .info("通知ID列表不能为空")
                        .build();
            }
            notificationService.batchDeleteNotifications(notificationIds);
            return ResponseEntity.<Void>builder()
                    .code(20000)
                    .info("批量删除通知成功")
                    .build();
        } catch (Exception e) {
            log.error("批量删除通知失败", e);
            return ResponseEntity.<Void>builder()
                    .code(20001)
                    .info("批量删除通知失败：" + e.getMessage())
                    .build();
        }
    }
}
