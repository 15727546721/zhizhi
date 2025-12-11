package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户反馈实体
 *
 * @author xu
 * @since 2024-12-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    // ==================== 反馈类型常量 ====================
    
    /** 类型：Bug问题 */
    public static final int TYPE_BUG = 0;
    /** 类型：功能建议 */
    public static final int TYPE_SUGGESTION = 1;
    /** 类型：内容问题 */
    public static final int TYPE_CONTENT = 2;
    /** 类型：其他 */
    public static final int TYPE_OTHER = 3;

    // ==================== 状态常量 ====================
    
    /** 状态：待处理 */
    public static final int STATUS_PENDING = 0;
    /** 状态：处理中 */
    public static final int STATUS_PROCESSING = 1;
    /** 状态：已解决 */
    public static final int STATUS_RESOLVED = 2;
    /** 状态：已关闭 */
    public static final int STATUS_CLOSED = 3;

    // ==================== 字段 ====================

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 反馈类型: 0-Bug 1-建议 2-内容问题 3-其他 */
    private Integer type;

    /** 标题 */
    private String title;

    /** 详细内容 */
    private String content;

    /** 图片URL，多个用逗号分隔 */
    private String images;

    /** 联系方式 */
    private String contact;

    /** 状态: 0-待处理 1-处理中 2-已解决 3-已关闭 */
    private Integer status;

    /** 管理员回复 */
    private String reply;

    /** 回复时间 */
    private LocalDateTime replyTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ==================== 工具方法 ====================

    /**
     * 获取类型名称
     */
    public String getTypeName() {
        if (type == null) return "未知";
        switch (type) {
            case TYPE_BUG: return "Bug问题";
            case TYPE_SUGGESTION: return "功能建议";
            case TYPE_CONTENT: return "内容问题";
            case TYPE_OTHER: return "其他";
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
            case STATUS_PROCESSING: return "处理中";
            case STATUS_RESOLVED: return "已解决";
            case STATUS_CLOSED: return "已关闭";
            default: return "未知";
        }
    }
}
