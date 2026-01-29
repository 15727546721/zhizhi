package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Feedback;
import cn.xu.repository.mapper.FeedbackMapper;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户反馈控制器（用户端）
 *
 * @author xu
 * @since 2024-12-09
 */
@Slf4j
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Tag(name = "用户反馈", description = "用户反馈相关接口")
public class FeedbackController {

    private final FeedbackMapper feedbackMapper;

    /**
     * 提交反馈
     */
    @PostMapping("/submit")
    @SaCheckLogin
    @Operation(summary = "提交反馈")
    @ApiOperationLog(description = "提交反馈")
    public ResponseEntity<Long> submitFeedback(@RequestBody FeedbackRequest request) {
        Long userId = LoginUserUtil.getLoginUserId();
        log.info("[反馈] 用户{}提交反馈: type={}, title={}", userId, request.getType(), request.getTitle());

        // 参数校验
        if (request.getType() == null || request.getType() < 0 || request.getType() > 3) {
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("请选择反馈类型")
                    .build();
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("请填写反馈标题")
                    .build();
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("请填写反馈内容")
                    .build();
        }

        // 创建反馈
        Feedback feedback = Feedback.builder()
                .userId(userId)
                .type(request.getType())
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .images(request.getImages())
                .contact(request.getContact())
                .status(Feedback.STATUS_PENDING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        feedbackMapper.insert(feedback);
        log.info("[反馈] 反馈提交成功: id={}", feedback.getId());

        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("反馈提交成功，感谢您的反馈！")
                .data(feedback.getId())
                .build();
    }

    /**
     * 获取我的反馈列表
     */
    @GetMapping("/my")
    @SaCheckLogin
    @Operation(summary = "我的反馈列表")
    @ApiOperationLog(description = "获取我的反馈列表")
    public ResponseEntity<PageResponse<List<FeedbackVO>>> getMyFeedbackList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = LoginUserUtil.getLoginUserId();
        int offset = (page - 1) * size;

        List<Feedback> feedbacks = feedbackMapper.selectByUserId(userId, offset, size);
        long total = feedbackMapper.countByUserId(userId);

        List<FeedbackVO> voList = feedbacks.stream()
                .map(FeedbackVO::fromFeedback)
                .collect(Collectors.toList());

        PageResponse<List<FeedbackVO>> pageResponse = PageResponse.ofList(page, size, total, voList);

        return ResponseEntity.<PageResponse<List<FeedbackVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(pageResponse)
                .build();
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/detail")
    @SaCheckLogin
    @Operation(summary = "反馈详情")
    @ApiOperationLog(description = "获取反馈详情")
    public ResponseEntity<FeedbackVO> getFeedbackDetail(@RequestParam Long id) {
        Long userId = LoginUserUtil.getLoginUserId();
        Feedback feedback = feedbackMapper.selectById(id);

        if (feedback == null || !feedback.getUserId().equals(userId)) {
            return ResponseEntity.<FeedbackVO>builder()
                    .code(ResponseCode.NOT_FOUND.getCode())
                    .info("反馈不存在")
                    .build();
        }

        return ResponseEntity.<FeedbackVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(FeedbackVO.fromFeedback(feedback))
                .build();
    }

    // ==================== 请求/响应类 ====================

    @Data
    public static class FeedbackRequest {
        /** 反馈类型: 0-Bug 1-建议 2-内容 3-其他 */
        private Integer type;
        /** 标题 */
        private String title;
        /** 详细内容 */
        private String content;
        /** 图片URL，多个用逗号分隔 */
        private String images;
        /** 联系方式 */
        private String contact;
    }

    @Data
    public static class FeedbackVO {
        private Long id;
        private Integer type;
        private String typeName;
        private String title;
        private String content;
        private String images;
        private String contact;
        private Integer status;
        private String statusName;
        private String reply;
        private LocalDateTime replyTime;
        private LocalDateTime createTime;

        public static FeedbackVO fromFeedback(Feedback feedback) {
            if (feedback == null) return null;
            FeedbackVO vo = new FeedbackVO();
            vo.setId(feedback.getId());
            vo.setType(feedback.getType());
            vo.setTypeName(feedback.getTypeName());
            vo.setTitle(feedback.getTitle());
            vo.setContent(feedback.getContent());
            vo.setImages(feedback.getImages());
            vo.setContact(feedback.getContact());
            vo.setStatus(feedback.getStatus());
            vo.setStatusName(feedback.getStatusName());
            vo.setReply(feedback.getReply());
            vo.setReplyTime(feedback.getReplyTime());
            vo.setCreateTime(feedback.getCreateTime());
            return vo;
        }
    }
}
