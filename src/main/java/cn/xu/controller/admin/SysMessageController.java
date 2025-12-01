package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统消息管理控制器
 * 管理系统通知和消息
 * 
 * @author xu
 * @since 2025-12-01
 */
@Slf4j
@RestController
@RequestMapping("/api/system/message")
@Tag(name = "系统消息管理", description = "系统消息管理相关接口")
public class SysMessageController {

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

    @PostMapping("/send")
    @Operation(summary = "发送系统消息")
    @SaCheckLogin
    @SaCheckPermission("system:message:send")
    @ApiOperationLog(description = "发送系统消息")
    public ResponseEntity<Void> sendMessage(@RequestBody SystemMessageVO message) {
        log.info("发送系统消息: {}", message);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("发送成功")
                .build();
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
