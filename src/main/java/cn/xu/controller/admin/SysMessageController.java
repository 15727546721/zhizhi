package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Notification;
import cn.xu.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统消息管理控制器
 * 
 * <p>提供系统通知和消息的查询、发送、删除功能</p>
 * <p>需要登录并拥有相应权限：system:message:send</p>
 *
 * @author xu
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/system/message")
@RequiredArgsConstructor
@Tag(name = "系统消息管理", description = "系统消息管理相关接口")
public class SysMessageController {

    private final NotificationService notificationService;

    /**
     * 获取系统消息列表
     * 
     * <p>分页查询系统消息，支持按类型和状态筛选</p>
     * <p>需要system:message:list权限</p>
     * <p>需要system:message:list权限
     * 
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param type 消息类型，1-系统通知 2-公告 3-私信）
     * @param status 状态（0-未读 1-已读）
     * @return 分页的消息列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取系统消息列表")
    @SaCheckLogin
    @SaCheckPermission("system:message:list")
    @ApiOperationLog(description = "获取系统消息列表")
    public ResponseEntity<PageResponse<List<SystemMessageVO>>> getMessageList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        log.info("获取系统消息列表: pageNo={}, pageSize={}, type={}", pageNo, pageSize, type);
        
        // 暂时返回空列表
        List<SystemMessageVO> messages = new ArrayList<>();
        PageResponse<List<SystemMessageVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, 0L, messages);
        
        return ResponseEntity.<PageResponse<List<SystemMessageVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    /**
     * 删除系统消息
     * 
     * <p>批量删除系统消息
     * <p>需要system:message:delete权限
     * 
     * @param ids 消息ID列表
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除系统消息")
    @SaCheckLogin
    @SaCheckPermission("system:message:delete")
    @ApiOperationLog(description = "删除系统消息")
    public ResponseEntity<Void> deleteMessage(@RequestBody List<Long> ids) {
        log.info("删除系统消息: ids={}", ids);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    /**
     * 发送系统通知
     * 
     * <p>向指定用户发送系统通知，需要管理员权限
     * <p>需要system:message:send权限
     * 
     * @param request 发送请求
     * @return 发送结果
     */
    @PostMapping("/send")
    @Operation(summary = "发送系统通知")
    @SaCheckLogin
    @SaCheckPermission("system:message:send")
    @ApiOperationLog(description = "发送系统通知")
    public ResponseEntity<Void> sendSystemNotification(@RequestBody SendNotificationRequest request) {
        log.info("[管理端] 发送系统通知: title={}, receiverIds={}", request.getTitle(), request.getReceiverIds());
        
        // 发送给指定用户
        if (request.getReceiverIds() != null && !request.getReceiverIds().isEmpty()) {
            for (Long receiverId : request.getReceiverIds()) {
                Notification notification = Notification.createSystemNotification(
                        receiverId, request.getTitle(), request.getContent());
                notificationService.sendNotification(notification);
            }
            log.info("[管理端] 系统通知发送完成, 发送给{}个用户", request.getReceiverIds().size());
        }
        
        return ResponseEntity.success();
    }

    /**
     * 发送系统通知请求
     */
    @Data
    public static class SendNotificationRequest {
        /** 通知标题 */
        private String title;
        /** 通知内容 */
        private String content;
        /** 接收者ID列表（为空则发送给全部用户） */
        private List<Long> receiverIds;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemMessageVO {
        private Long id;
        private String title;
        private String content;
        private Integer type;          // 1-系统通知 2-公告 3-私信
        private Integer status;        // 0-未读 1-已读
        private Long senderId;
        private String senderName;
        private Long receiverId;
        private String receiverName;
        private LocalDateTime createTime;
        private LocalDateTime readTime;
    }
}