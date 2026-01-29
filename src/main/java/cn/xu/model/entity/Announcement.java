package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {

    /** 状态：草稿 */
    public static final int STATUS_DRAFT = 0;
    /** 状态：已发布 */
    public static final int STATUS_PUBLISHED = 1;
    /** 状态：已下架 */
    public static final int STATUS_OFFLINE = 2;

    /** 类型：普通公告 */
    public static final int TYPE_NORMAL = 0;
    /** 类型：活动公告 */
    public static final int TYPE_ACTIVITY = 1;
    /** 类型：系统公告 */
    public static final int TYPE_SYSTEM = 2;
    /** 类型：更新公告 */
    public static final int TYPE_UPDATE = 3;

    private Long id;

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 公告类型：0-普通 1-活动 2-系统 3-更新 */
    private Integer type;

    /** 状态：0-草稿 1-已发布 2-已下架 */
    private Integer status;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 发布者ID */
    private Long publisherId;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 获取类型名称 */
    public String getTypeName() {
        if (type == null) return "普通";
        switch (type) {
            case TYPE_ACTIVITY: return "活动";
            case TYPE_SYSTEM: return "系统";
            case TYPE_UPDATE: return "更新";
            default: return "普通";
        }
    }

    /** 获取状态名称 */
    public String getStatusName() {
        if (status == null) return "草稿";
        switch (status) {
            case STATUS_PUBLISHED: return "已发布";
            case STATUS_OFFLINE: return "已下架";
            default: return "草稿";
        }
    }
}
