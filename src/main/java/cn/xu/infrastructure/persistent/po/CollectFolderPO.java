package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹持久化对象
 */
@Data
public class CollectFolderPO {

    /**
     * 收藏夹ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏夹名称
     */
    private String name;

    /**
     * 收藏夹描述
     */
    private String description;

    /**
     * 是否为默认收藏夹（0-否，1-是）
     */
    private Integer isDefault;

    /**
     * 收藏内容数量
     */
    private Integer contentCount;

    /**
     * 是否公开（0-私密，1-公开）
     */
    private Integer isPublic;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}