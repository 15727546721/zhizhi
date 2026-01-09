package cn.xu.service.report;

import cn.xu.model.dto.report.HandleReportRequest;
import cn.xu.model.dto.report.ReportQueryRequest;
import cn.xu.model.dto.report.ReportRequest;
import cn.xu.model.entity.*;
import cn.xu.model.vo.report.ReportDetailVO;
import cn.xu.model.vo.report.ReportVO;
import cn.xu.repository.CommentRepository;
import cn.xu.repository.mapper.ReportMapper;
import cn.xu.service.notification.NotificationService;
import cn.xu.service.post.PostQueryService;
import cn.xu.service.post.PostCommandService;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 举报服务
 *
 * @author xu
 * @since 2025-12-08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final UserService userService;
    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    // ==================== 用户端接口 ====================

    /**
     * 提交举报
     */
    @Transactional
    public Long submitReport(Long reporterId, ReportRequest request) {
        // 1. 检查是否已举报过（待处理状态）
        int exists = reportMapper.checkExists(reporterId, request.getTargetType(), request.getTargetId());
        if (exists > 0) {
            throw new BusinessException("您已举报过该内容，请等待处理");
        }

        // 2. 获取被举报用户ID
        Long targetUserId = getTargetUserId(request.getTargetType(), request.getTargetId());
        if (targetUserId == null) {
            throw new BusinessException("举报目标不存在");
        }

        // 3. 不能举报自己
        if (reporterId.equals(targetUserId)) {
            throw new BusinessException("不能举报自己");
        }

        // 4. 构建举报记录
        Report report = Report.builder()
                .reporterId(reporterId)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .targetUserId(targetUserId)
                .reason(request.getReason())
                .description(request.getDescription())
                .evidenceUrls(request.getEvidenceUrls() != null ? JSON.toJSONString(request.getEvidenceUrls()) : null)
                .status(Report.STATUS_PENDING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        reportMapper.insert(report);
        log.info("[举报] 提交成功 - reportId: {}, reporter: {}, target: {}/{}",
                report.getId(), reporterId, request.getTargetType(), request.getTargetId());

        return report.getId();
    }

    /**
     * 获取用户的举报记录
     */
    public List<ReportVO> getMyReports(Long reporterId, int page, int size) {
        int offset = (page - 1) * size;
        List<Report> reports = reportMapper.selectByReporterId(reporterId, offset, size);

        return reports.stream()
                .map(report -> {
                    ReportVO vo = ReportVO.from(report);
                    // 填充目标简介
                    vo.setTargetSummary(getTargetSummary(report.getTargetType(), report.getTargetId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 统计用户的举报数量
     */
    public long countMyReports(Long reporterId) {
        return reportMapper.countByReporterId(reporterId);
    }

    /**
     * 获取举报详情（用户端）
     */
    public ReportVO getReportDetail(Long reporterId, Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        // 只能查看自己的举报
        if (!report.getReporterId().equals(reporterId)) {
            throw new BusinessException("无权查看该举报");
        }
        ReportVO vo = ReportVO.from(report);
        vo.setTargetSummary(getTargetSummary(report.getTargetType(), report.getTargetId()));
        return vo;
    }

    // ==================== 管理端接口 ====================

    /**
     * 分页查询举报列表
     */
    public List<ReportDetailVO> queryReports(ReportQueryRequest request) {
        int offset = (request.getPageNo() - 1) * request.getPageSize();
        List<Report> reports = reportMapper.selectReportList(
                request.getStatus(),
                request.getTargetType(),
                request.getReason(),
                request.getReporterId(),
                request.getTargetUserId(),
                request.getKeyword(),
                offset,
                request.getPageSize()
        );

        if (reports.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量获取用户信息
        Set<Long> userIds = new HashSet<>();
        reports.forEach(r -> {
            userIds.add(r.getReporterId());
            userIds.add(r.getTargetUserId());
            if (r.getHandlerId() != null) {
                userIds.add(r.getHandlerId());
            }
        });
        Map<Long, User> userMap = getUserMap(userIds);

        return reports.stream()
                .map(report -> buildReportDetailVO(report, userMap))
                .collect(Collectors.toList());
    }

    /**
     * 统计举报数量
     */
    public long countReports(ReportQueryRequest request) {
        return reportMapper.countReportList(
                request.getStatus(),
                request.getTargetType(),
                request.getReason(),
                request.getReporterId(),
                request.getTargetUserId(),
                request.getKeyword()
        );
    }

    /**
     * 获取举报详情（管理端）
     */
    public ReportDetailVO getReportDetailForAdmin(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }

        Set<Long> userIds = new HashSet<>();
        userIds.add(report.getReporterId());
        userIds.add(report.getTargetUserId());
        if (report.getHandlerId() != null) {
            userIds.add(report.getHandlerId());
        }
        Map<Long, User> userMap = getUserMap(userIds);

        ReportDetailVO vo = buildReportDetailVO(report, userMap);

        // 解析证据URL
        if (report.getEvidenceUrls() != null && !report.getEvidenceUrls().isEmpty()) {
            try {
                vo.setEvidenceUrls(JSON.parseArray(report.getEvidenceUrls(), String.class));
            } catch (Exception e) {
                log.warn("[举报] 解析证据URL失败: {}", report.getEvidenceUrls());
            }
        }

        return vo;
    }

    /**
     * 处理举报
     */
    @Transactional
    public void handleReport(Long handlerId, Long reportId, HandleReportRequest request) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        if (report.isHandled()) {
            throw new BusinessException("该举报已处理");
        }

        // 更新举报状态
        report.setStatus(request.getStatus());
        report.setHandlerId(handlerId);
        report.setHandleResult(request.getHandleResult());
        report.setHandleAction(request.getHandleAction());
        report.setHandleTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        reportMapper.updateById(report);

        // 如果通过举报，执行处罚
        if (Report.STATUS_APPROVED == request.getStatus() && request.getHandleAction() != null) {
            executeAction(report, request.getHandleAction());
        }

        // 通知举报人处理结果
        sendResultNotification(report);

        log.info("[举报] 处理完成 - reportId: {}, handler: {}, status: {}, action: {}",
                reportId, handlerId, request.getStatus(), request.getHandleAction());
    }

    /**
     * 批量忽略举报
     */
    @Transactional
    public int batchIgnore(Long handlerId, List<Long> reportIds) {
        if (reportIds == null || reportIds.isEmpty()) {
            return 0;
        }
        return reportMapper.batchIgnore(reportIds, handlerId);
    }

    /**
     * 获取举报统计
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Map<String, Object>> statusCounts = reportMapper.countByStatus();
        long total = 0;
        long pending = 0;

        for (Map<String, Object> item : statusCounts) {
            Integer status = ((Number) item.get("status")).intValue();
            Long count = ((Number) item.get("count")).longValue();
            total += count;
            if (status == Report.STATUS_PENDING) {
                pending = count;
            }
        }

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("handled", total - pending);

        return stats;
    }

    // ==================== 私有方法 ====================

    /**
     * 获取被举报用户ID
     */
    private Long getTargetUserId(Integer targetType, Long targetId) {
        switch (targetType) {
            case Report.TARGET_TYPE_POST:
                return postQueryService.getById(targetId)
                        .map(Post::getUserId)
                        .orElse(null);
            case Report.TARGET_TYPE_COMMENT:
                Comment comment = commentRepository.findById(targetId);
                return comment != null ? comment.getUserId() : null;
            case Report.TARGET_TYPE_USER:
                User user = userService.getUserInfo(targetId);
                return user != null ? targetId : null;
            default:
                return null;
        }
    }

    /**
     * 获取目标简介
     */
    private String getTargetSummary(Integer targetType, Long targetId) {
        try {
            switch (targetType) {
                case Report.TARGET_TYPE_POST:
                    return postQueryService.getById(targetId)
                            .map(Post::getTitle)
                            .orElse("[帖子已删除]");
                case Report.TARGET_TYPE_COMMENT:
                    Comment comment = commentRepository.findById(targetId);
                    if (comment == null) return "[评论已删除]";
                    String content = comment.getContent();
                    return content.length() > 50 ? content.substring(0, 50) + "..." : content;
                case Report.TARGET_TYPE_USER:
                    User user = userService.getUserInfo(targetId);
                    return user != null ? user.getNickname() : "[用户不存在]";
                default:
                    return "未知";
            }
        } catch (Exception e) {
            return "[获取失败]";
        }
    }

    /**
     * 批量获取用户信息
     */
    private Map<Long, User> getUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) return Collections.emptyMap();
        return userIds.stream()
                .map(id -> {
                    try {
                        return userService.getUserInfo(id);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    /**
     * 构建举报详情VO
     */
    private ReportDetailVO buildReportDetailVO(Report report, Map<Long, User> userMap) {
        ReportDetailVO vo = ReportDetailVO.from(report);

        // 举报人信息
        User reporter = userMap.get(report.getReporterId());
        if (reporter != null) {
            vo.setReporterNickname(reporter.getNickname());
            vo.setReporterAvatar(reporter.getAvatar());
        }

        // 被举报用户信息
        User targetUser = userMap.get(report.getTargetUserId());
        if (targetUser != null) {
            vo.setTargetUserNickname(targetUser.getNickname());
            vo.setTargetUserAvatar(targetUser.getAvatar());
        }

        // 处理人信息
        if (report.getHandlerId() != null) {
            User handler = userMap.get(report.getHandlerId());
            if (handler != null) {
                vo.setHandlerNickname(handler.getNickname());
            }
        }

        // 目标内容
        vo.setTargetContent(getTargetSummary(report.getTargetType(), report.getTargetId()));

        return vo;
    }

    /**
     * 执行处罚措施
     */
    private void executeAction(Report report, Integer action) {
        if (action == null || action == Report.ACTION_NONE) {
            return;
        }

        try {
            switch (action) {
                case Report.ACTION_DELETE:
                    // 删除内容
                    if (report.getTargetType() == Report.TARGET_TYPE_POST) {
                        // 管理员删除，isAdmin=true
                        postCommandService.deletePost(report.getTargetId(), null, true);
                        log.info("[举报] 删除帖子: {}", report.getTargetId());
                    } else if (report.getTargetType() == Report.TARGET_TYPE_COMMENT) {
                        commentRepository.deleteById(report.getTargetId());
                        log.info("[举报] 删除评论: {}", report.getTargetId());
                    }
                    break;
                case Report.ACTION_WARN:
                    // 发送警告通知
                    sendWarningNotification(report.getTargetUserId(), "您发布的内容因违规被举报，请注意规范言行。");
                    break;
                case Report.ACTION_BAN_7D:
                case Report.ACTION_BAN_30D:
                case Report.ACTION_BAN_FOREVER:
                    // 禁言/封号（更新用户状态）
                    userService.banUser(report.getTargetUserId());
                    log.info("[举报] 禁用用户: {}, action: {}", report.getTargetUserId(), action);
                    break;
            }
        } catch (Exception e) {
            log.error("[举报] 执行处罚失败 - reportId: {}, action: {}", report.getId(), action, e);
        }
    }

    /**
     * 发送警告通知
     */
    private void sendWarningNotification(Long userId, String content) {
        Notification notification = Notification.builder()
                .type(Notification.TYPE_SYSTEM)
                .receiverId(userId)
                .senderType(Notification.SENDER_TYPE_SYSTEM)
                .content(content)
                .businessType(Notification.BUSINESS_SYSTEM)
                .isRead(Notification.READ_NO)
                .status(Notification.STATUS_VALID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);
    }

    /**
     * 发送举报处理结果通知
     */
    private void sendResultNotification(Report report) {
        String content;
        if (report.getStatus() == Report.STATUS_APPROVED) {
            content = "您的举报已通过审核，感谢您对社区的贡献。";
        } else if (report.getStatus() == Report.STATUS_REJECTED) {
            content = "您的举报未通过审核" + (report.getHandleResult() != null ? "：" + report.getHandleResult() : "。");
        } else {
            return; // 忽略状态不通知
        }

        Notification notification = Notification.builder()
                .type(Notification.TYPE_SYSTEM)
                .receiverId(report.getReporterId())
                .senderType(Notification.SENDER_TYPE_SYSTEM)
                .content(content)
                .businessType(Notification.BUSINESS_SYSTEM)
                .isRead(Notification.READ_NO)
                .status(Notification.STATUS_VALID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        notificationService.sendNotification(notification);
    }
}
