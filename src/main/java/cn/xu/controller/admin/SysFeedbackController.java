package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Feedback;
import cn.xu.model.entity.User;
import cn.xu.repository.mapper.FeedbackMapper;
import cn.xu.repository.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 反馈管理控制器（管理端）
 * 
 * <p>只使用 GET 和 POST 方法</p>
 *
 * @author xu
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/system/feedback")
@RequiredArgsConstructor
@Tag(name = "反馈管理", description = "管理端反馈管理接口")
public class SysFeedbackController {

    private final FeedbackMapper feedbackMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取反馈列表
     */
    @GetMapping("/list")
    @SaCheckLogin
    @Operation(summary = "获取反馈列表")
    @ApiOperationLog(description = "获取反馈列表")
    public ResponseEntity<PageResponse<List<FeedbackAdminVO>>> getFeedbackList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        log.info("[管理端] 获取反馈列表: pageNo={}, pageSize={}, type={}, status={}", pageNo, pageSize, type, status);

        int offset = (pageNo - 1) * pageSize;
        List<Feedback> feedbacks = feedbackMapper.selectFeedbackList(type, status, offset, pageSize);
        long total = feedbackMapper.countFeedback(type, status);

        List<FeedbackAdminVO> voList = feedbacks.stream()
                .map(this::toAdminVO)
                .collect(Collectors.toList());

        PageResponse<List<FeedbackAdminVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, total, voList);

        return ResponseEntity.<PageResponse<List<FeedbackAdminVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(pageResponse)
                .build();
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/detail")
    @SaCheckLogin
    @Operation(summary = "获取反馈详情")
    @ApiOperationLog(description = "获取反馈详情")
    public ResponseEntity<FeedbackAdminVO> getFeedbackDetail(@RequestParam Long id) {
        log.info("[管理端] 获取反馈详情: id={}", id);

        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            return ResponseEntity.<FeedbackAdminVO>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("反馈不存在")
                    .build();
        }

        return ResponseEntity.<FeedbackAdminVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(toAdminVO(feedback))
                .build();
    }

    /**
     * 回复反馈
     */
    @PostMapping("/reply")
    @SaCheckLogin
    @SaCheckPermission("system:feedback:update")
    @Operation(summary = "回复反馈")
    @ApiOperationLog(description = "回复反馈")
    public ResponseEntity<Void> replyFeedback(@RequestBody ReplyRequest request) {
        log.info("[管理端] 回复反馈: id={}", request.getId());

        Feedback feedback = feedbackMapper.selectById(request.getId());
        if (feedback == null) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("反馈不存在")
                    .build();
        }

        feedback.setReply(request.getReply());
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setStatus(Feedback.STATUS_RESOLVED);
        feedback.setUpdateTime(LocalDateTime.now());
        feedbackMapper.updateById(feedback);

        log.info("[管理端] 反馈回复成功: id={}", request.getId());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("回复成功")
                .build();
    }

    /**
     * 修改反馈状态
     */
    @PostMapping("/status")
    @SaCheckLogin
    @SaCheckPermission("system:feedback:update")
    @Operation(summary = "修改反馈状态")
    @ApiOperationLog(description = "修改反馈状态")
    public ResponseEntity<Void> updateStatus(@RequestBody StatusRequest request) {
        log.info("[管理端] 修改反馈状态: id={}, status={}", request.getId(), request.getStatus());

        Feedback feedback = feedbackMapper.selectById(request.getId());
        if (feedback == null) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("反馈不存在")
                    .build();
        }

        feedback.setStatus(request.getStatus());
        feedback.setUpdateTime(LocalDateTime.now());
        feedbackMapper.updateById(feedback);

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("状态更新成功")
                .build();
    }

    /**
     * 删除反馈
     */
    @PostMapping("/delete")
    @SaCheckLogin
    @SaCheckPermission("system:feedback:delete")
    @Operation(summary = "删除反馈")
    @ApiOperationLog(description = "删除反馈")
    public ResponseEntity<Void> deleteFeedback(@RequestBody DeleteRequest request) {
        log.info("[管理端] 删除反馈: ids={}", request.getIds());

        if (request.getIds() != null && !request.getIds().isEmpty()) {
            feedbackMapper.deleteBatchIds(request.getIds());
        }

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    // ==================== 转换方法 ====================

    private FeedbackAdminVO toAdminVO(Feedback feedback) {
        FeedbackAdminVO vo = new FeedbackAdminVO();
        vo.setId(feedback.getId());
        vo.setUserId(feedback.getUserId());
        vo.setType(feedback.getType());
        vo.setTypeName(feedback.getTypeName());
        vo.setTitle(feedback.getTitle());
        vo.setContent(feedback.getContent());
        vo.setImages(feedback.getImages());
        vo.setContact(feedback.getContact());
        vo.setStatus(feedback.getStatus());
        vo.setStatusName(feedback.getStatusName());
        vo.setReply(feedback.getReply());
        if (feedback.getReplyTime() != null) {
            vo.setReplyTime(feedback.getReplyTime().format(FORMATTER));
        }
        if (feedback.getCreateTime() != null) {
            vo.setCreateTime(feedback.getCreateTime().format(FORMATTER));
        }

        // 获取用户信息
        User user = userMapper.selectById(feedback.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        return vo;
    }

    // ==================== 请求/响应类 ====================

    @Data
    public static class ReplyRequest {
        private Long id;
        private String reply;
    }

    @Data
    public static class StatusRequest {
        private Long id;
        private Integer status;
    }

    @Data
    public static class DeleteRequest {
        private List<Long> ids;
    }

    @Data
    public static class FeedbackAdminVO {
        private Long id;
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private Integer type;
        private String typeName;
        private String title;
        private String content;
        private String images;
        private String contact;
        private Integer status;
        private String statusName;
        private String reply;
        private String replyTime;
        private String createTime;
    }
}
