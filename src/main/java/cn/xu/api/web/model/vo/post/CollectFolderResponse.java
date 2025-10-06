package cn.xu.api.web.model.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹Response对象
 */
@Data
@Schema(description = "收藏夹信息")
public class CollectFolderResponse {

    @Schema(description = "收藏夹ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "收藏夹名称")
    private String name;

    @Schema(description = "收藏夹描述")
    private String description;

    @Schema(description = "是否为默认收藏夹（0-否，1-是）")
    private Integer isDefault;

    @Schema(description = "收藏帖子数量")
    private Integer postCount;

    @Schema(description = "是否公开（0-私密，1-公开）")
    private Integer isPublic;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}