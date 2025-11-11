package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息表
 * @TableName message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
    /**
     * 消息ID
     */
    private Long id;

    /**
     * 消息类型：1-系统消息/公告 2-私信消息 3-点赞消息 4-收藏消息 5-评论消息 6-关注消息
     */
    private Integer type;

    /**
     * 发送者ID（系统消息时为null）
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息标题（系统消息/公告必填）
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 目标ID（如：文章ID、评论ID、用户ID等）
     */
    private Long targetId;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 消息状态：1-正常（对方可收到） 2-未送达（对方收不到，仅数据库记录）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}