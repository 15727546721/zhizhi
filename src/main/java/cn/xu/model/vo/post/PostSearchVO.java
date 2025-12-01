package cn.xu.model.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帖子搜索结果VO
 * 用于搜索结果展示，包含搜索相关的特殊字段
 * 
 * 使用场景：
 * - 全文搜索结果
 * - ElasticSearch搜索结果
 * 
 * @author zhizhi
 * @since 2025-11-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子搜索结果VO")
public class PostSearchVO {
    
    // ========== 基础帖子信息 ==========
    
    @Schema(description = "帖子基本信息")
    private PostItemVO post;
    
    // ========== 搜索相关字段 ==========
    
    @Schema(description = "高亮标题（包含<em>标签）", example = "如何学习<em>Java</em>")
    private String highlightTitle;
    
    @Schema(description = "高亮内容片段（包含<em>标签）", example = "本文介绍<em>Java</em>学习路线...")
    private String highlightContent;
    
    @Schema(description = "高亮描述（包含<em>标签）")
    private String highlightDescription;
    
    @Schema(description = "搜索得分（相关度）", example = "95.5")
    private Float score;
    
    @Schema(description = "匹配的标签名称")
    private String[] matchedTags;
    
    @Schema(description = "搜索来源", example = "title",
            allowableValues = {"title", "content", "tag", "author"})
    private String matchSource;
}
