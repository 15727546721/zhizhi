package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 举报实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    private Long id;

    /**
     * 举报人ID
     */
    private Long reporterId;

    // ==================== 举报目标 ====================

    /**
     * 目标类型: 1-帖子 2-评论 3-用户
     */
    private Integer targetType;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 被举报用户ID
     */
    private Long targetUserId;

    // ==================== 举报内容 ====================

    /**
     * 举报原因: 1-垃圾广告 2-违法违规 3-色情低俗 4-人身攻击 5-抄袭侵权 6-其他
     */
    private Integer reason;

    /**
     * 详细说明
     */
    private String description;

    /**
     * 截图证据URL(JSON数组)
     */
    private String evidenceUrls;

    // ==================== 处理信息 ====================

    /**
     * 状态: 0-待处理 1-已通过 2-已驳回 3-已忽略
     */
    private Integer status;

    /**
     * 处理人ID
     */
    private Long handlerId;

    /**
     * 处理结果说明
     */
    private String handleResult;

    /**
     * 处罚措施: 0-无 1-删除内容 2-警告 3-禁言7天 4-禁言30天 5-永久封号
     */
    private Integer handleAction;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // ==================== 常量定义 ====================

    // 目标类型
    public static final int TARGET_TYPE_POST = 1;
    public static final int TARGET_TYPE_COMMENT = 2;
    public static final int TARGET_TYPE_USER = 3;

    // 举报原因
    public static final int REASON_SPAM = 1;           // 垃圾广告
    public static final int REASON_ILLEGAL = 2;        // 违法违规
    public static final int REASON_PORN = 3;           // 色情低俗
    public static final int REASON_ATTACK = 4;         // 人身攻击
    public static final int REASON_PLAGIARISM = 5;     // 抄袭侵权
    public static final int REASON_OTHER = 6;          // 其他

    // 状态
    public static final int STATUS_PENDING = 0;        // 待处理
    public static final int STATUS_APPROVED = 1;       // 已通过
    public static final int STATUS_REJECTED = 2;       // 已驳回
    public static final int STATUS_IGNORED = 3;        // 已忽略

    // 处罚措施
    public static final int ACTION_NONE = 0;           // 无
    public static final int ACTION_DELETE = 1;         // 删除内容
    public static final int ACTION_WARN = 2;           // 警告
    public static final int ACTION_BAN_7D = 3;         // 禁言7天
    public static final int ACTION_BAN_30D = 4;        // 禁言30天
    public static final int ACTION_BAN_FOREVER = 5;    // 永久封号

    // ==================== 业务方法 ====================

    /**
     * 是否待处理
     */
    public boolean isPending() {
        return STATUS_PENDING == this.status;
    }

    /**
     * 是否已处理
     */
    public boolean isHandled() {
        return this.status != null && this.status > 0;
    }

    /**
     * 获取目标类型名称
     */
    public String getTargetTypeName() {
        if (targetType == null) return "未知";
        switch (targetType) {
            case TARGET_TYPE_POST: return "帖子";
            case TARGET_TYPE_COMMENT: return "评论";
            case TARGET_TYPE_USER: return "用户";
            default: return "未知";
        }
    }

    /**
     * 获取举报原因名称
     */
    public String getReasonName() {
        if (reason == null) return "未知";
        switch (reason) {
            case REASON_SPAM: return "垃圾广告";
            case REASON_ILLEGAL: return "违法违规";
            case REASON_PORN: return "色情低俗";
            case REASON_ATTACK: return "人身攻击";
            case REASON_PLAGIARISM: return "抄袭侵权";
            case REASON_OTHER: return "其他";
            default: return "未知";
        }
    }

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        if (status == null) return "未知";
        switch (status) {
            case STATUS_PENDING: return "待处理";
            case STATUS_APPROVED: return "已通过";
            case STATUS_REJECTED: return "已驳回";
            case STATUS_IGNORED: return "已忽略";
            default: return "未知";
        }
    }

    /**
     * 获取处罚措施名称
     */
    public String getHandleActionName() {
        if (handleAction == null) return "无";
        switch (handleAction) {
            case ACTION_NONE: return "无";
            case ACTION_DELETE: return "删除内容";
            case ACTION_WARN: return "警告";
            case ACTION_BAN_7D: return "禁言7天";
            case ACTION_BAN_30D: return "禁言30天";
            case ACTION_BAN_FOREVER: return "永久封号";
            default: return "未知";
        }
    }
}
