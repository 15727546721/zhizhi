package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.notification.NotificationRequest;
import cn.xu.model.entity.Notification;
import cn.xu.model.vo.notification.NotificationVO;
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
 * 通知管理控制器
 * 
 * <p>提供通知相关API：
 * <ul>
 *   <li>通知类型：0-系统 1-点赞 2-收藏 3-评论 4-回复 5-关注 6-@提及</li>
 *   <li>支持按类型查询、标记已读、删除等操作</li>
 * </ul>
 *
 * @author xu
 * @since 1.0
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知相关接口")
public class NotificationController {

    private final NotificationService notificationService;

    // ==================== 查询接口 ====================

    /**
     * 获取用户通知列表
     * 
     * @param request 查询参数（type支持逗号分隔多类型，如"3,4"表示评论+回复）
     */
    @GetMapping
    @Operation(summary = "获取用户通知列表")
    @ApiOperationLog(description = "获取用户通知列表")
    public ResponseEntity<PageResponse<List<NotificationVO>>> list(
            @Validated NotificationRequest request) {
        Long userId = currentUserId();
        
        // 查询通知列表
        List<Notification> notifications = notificationService.getUserNotifications(
                userId, request.getType(), request.getPageNo(), request.getPageSize());
        
        // 转换为VO
        List<NotificationVO> voList = notifications.stream()
                .map(NotificationVO::from)
                .collect(Collectors.toList());
        
        // 查询总数（用于分页）
        long total = notificationService.countByUserIdAndType(userId, request.getType());
        
        return ResponseEntity.success(
                PageResponse.of(request.getPageNo(), request.getPageSize(), total, voList));
    }

    /**
     * 获取未读通知总数
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读通知总数")
    @ApiOperationLog(description = "获取未读通知总数")
    public ResponseEntity<Long> unreadCount() {
        long count = notificationService.getUnreadCount(currentUserId());
        return ResponseEntity.success(count);
    }

    /**
     * 获取各类型未读通知数量
     * 
     * @return Map<类型, 数量>，如 {1: 5, 3: 2} 表示点赞5条、评论2条
     */
    @GetMapping("/unread/count-by-type")
    @Operation(summary = "获取各类型未读通知数量")
    @ApiOperationLog(description = "获取各类型未读通知数量")
    public ResponseEntity<Map<Integer, Long>> unreadCountByType() {
        Map<Integer, Long> counts = notificationService.getUnreadCountByType(currentUserId());
        return ResponseEntity.success(counts);
    }

    // ==================== 已读操作 ====================

    /**
     * 标记单个通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读")
    @ApiOperationLog(description = "标记通知为已读")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        notificationService.markAsRead(currentUserId(), id);
        return ResponseEntity.success();
    }

    /**
     * 标记全部通知为已读（可按类型）
     * 
     * @param type 通知类型（可选，不传则标记全部）
     */
    @PutMapping("/read-all")
    @Operation(summary = "标记全部通知为已读")
    @ApiOperationLog(description = "标记全部通知为已读")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "通知类型，不传则标记全部") @RequestParam(required = false) Integer type) {
        notificationService.markAllAsRead(currentUserId(), type);
        return ResponseEntity.success();
    }

    // ==================== 删除操作 ====================

    /**
     * 删除单个通知
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知")
    @ApiOperationLog(description = "删除通知")
    public ResponseEntity<Void> delete(
            @Parameter(description = "通知ID") @PathVariable Long id) {
        notificationService.delete(currentUserId(), id);
        return ResponseEntity.success();
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除通知")
    @ApiOperationLog(description = "批量删除通知")
    public ResponseEntity<Void> batchDelete(
            @RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.error("通知ID列表不能为空");
        }
        notificationService.batchDelete(currentUserId(), ids);
        return ResponseEntity.success();
    }

    // ==================== 私有方法 ====================

    /**
     * 获取当前登录用户ID
     */
    private Long currentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
}
