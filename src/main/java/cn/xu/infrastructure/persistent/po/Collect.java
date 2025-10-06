package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏持久化对象
 */
@Data
public class Collect {

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 被收藏内容ID
     */
    private Long targetId;

    /**
     * 所属收藏夹ID，如果为空，表示是普通收藏
     */
    private Long folderId;

    /**
     * 收藏内容类型
     */
    private String targetType;

    /**
     * 收藏状态：1-收藏，0-未收藏
     */
    private Integer status;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}