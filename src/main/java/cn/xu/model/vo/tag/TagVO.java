package cn.xu.model.vo.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签VO
 * 用于标签信息展示
 * 
 * 使用场景：
 * - 标签列表
 * - 热门标签
 * - 帖子标签
 * - 标签搜索结果
 * - 用户感兴趣的标签
 * 
 * @author zhizhi
 * @since 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "标签VO")
public class TagVO {
    
    // ========== 基础信息 ==========
    
    @Schema(description = "标签ID", example = "1")
    private Long id;
    
    @Schema(description = "标签名称", example = "Java")
    private String name;
    
    @Schema(description = "标签描述", example = "Java编程语言相关内容")
    private String description;
    
    // ========== 统计信息 ==========
    
    @Schema(description = "使用次数（帖子数量）", example = "1000")
    private Long usageCount;
    
    @Schema(description = "是否为推荐标签", example = "true")
    private Boolean isRecommended;
    
    // ========== 时间信息 ==========
    
    @Schema(description = "创建时间", example = "2025-01-01T10:00:00")
    private LocalDateTime createTime;
    
    @Schema(description = "最后使用时间", example = "2025-11-24T10:00:00")
    private LocalDateTime lastUsedTime;
    

}
