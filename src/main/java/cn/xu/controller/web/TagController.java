package cn.xu.controller.web;

import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.Tag;
import cn.xu.repository.IPostTagRepository;
import cn.xu.service.post.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * 标签控制器
 * 
 * <p>提供标签查询、搜索、热门标签、标签统计等功能接口
 * 
 *
 */
@Slf4j
@RestController
@RequestMapping("/api/tag")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签接口", description = "标签查询、搜索、统计等API")
public class TagController {

    @Resource(name = "tagService")
    private TagService tagService;

    @Resource
    private IPostTagRepository postTagRepository;

    /**
     * 获取标签列表
     * 
     * <p>获取系统中所有可用的标签列表
     * <p>公开接口，无需登录
     * 
     * @return 标签列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取标签列表", description = "获取所有可用的标签列表")
    @ApiOperationLog(description = "获取标签列表")
    public ResponseEntity<List<Tag>> getTagList() {
        try {
            List<Tag> TagList = tagService.getTagList();
            return ResponseEntity.<List<Tag>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询标签列表成功")
                    .data(TagList)
                    .build();
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return ResponseEntity.<List<Tag>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取标签列表失败")
                    .build();
        }
    }

    /**
     * 搜索标签
     * 
     * <p>根据关键词模糊搜索标签名称和描述
     * <p>公开接口，无需登录
     * 
     * @param keyword 搜索关键词
     * @return 匹配的标签列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索标签", description = "根据关键词搜索标签")
    @ApiOperationLog(description = "搜索标签")
    public ResponseEntity<List<Tag>> searchTags(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            List<Tag> TagList = tagService.searchTags(keyword);
            return ResponseEntity.<List<Tag>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索标签成功")
                    .data(TagList)
                    .build();
        } catch (Exception e) {
            log.error("搜索标签失败", e);
            return ResponseEntity.<List<Tag>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("搜索标签失败")
                    .build();
        }
    }

    /**
     * 获取热门标签
     * 
     * <p>根据使用频率获取热门标签，支持按时间维度筛选
     * <p>公开接口，无需登录
     * 
     * @param timeRange 时间范围（可选）：today(今日)、week(本周)、month(本月)、all(全部)，默认all
     * @param limit 返回数量限制，默认为10
     * @return 热门标签列表，按使用次数降序排列
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门标签", description = "获取使用频率最高的标签，支持时间维度查询")
    @ApiOperationLog(description = "获取热门标签")
    public ResponseEntity<List<Tag>> getHotTags(
            @Parameter(description = "时间范围：today(今日)、week(本周)、month(本月)、all(全部)") 
            @RequestParam(required = false, defaultValue = "all") String timeRange,
            @Parameter(description = "返回数量限制") 
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<Tag> TagList = tagService.getHotTagsByTimeRange(timeRange, limit);
            // 确保不返回null
            if (TagList == null) {
                TagList = new java.util.ArrayList<>();
            }
            log.debug("获取热门标签成功: timeRange={}, limit={}, count={}", timeRange, limit, TagList.size());
            return ResponseEntity.<List<Tag>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取热门标签成功")
                    .data(TagList)
                    .build();
        } catch (Exception e) {
            log.error("获取热门标签失败, timeRange: {}, limit: {}", timeRange, limit, e);
            return ResponseEntity.<List<Tag>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取热门标签失败: " + e.getMessage())
                    .data(new java.util.ArrayList<>())
                    .build();
        }
    }

    /**
     * 获取标签相关帖子
     * 
     * <p>分页获取使用指定标签的所有帖子
     * <p>公开接口，无需登录
     * 
     * @param tagId 标签ID
     * @param pageNo 页码，从1开始，默认为1
     * @param pageSize 每页数量，默认为20
     * @return 分页的帖子列表
     */
    @GetMapping("/{tagId}/posts")
    @Operation(summary = "获取标签相关帖子", description = "获取使用指定标签的帖子列表")
    @ApiOperationLog(description = "获取标签相关帖子")
    public ResponseEntity<PageResponse<List<Post>>> getTagPosts(
            @Parameter(description = "标签ID") @PathVariable Long tagId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            if (tagId == null) {
                return ResponseEntity.<PageResponse<List<Post>>>builder()
                        .code(ResponseCode.PARAM_ERROR.getCode())
                        .info("标签ID不能为空")
                        .build();
            }
            
            // 计算offset
            int offset = Math.max(0, (pageNo - 1) * pageSize);
            
            // 获取标签相关的帖子列表
            List<Post> posts = postTagRepository.getPostsByTagId(tagId, offset, pageSize);
            
            // 统计总数（简化处理，实际应该单独查询）
            long total = posts.size();
            
            // 构建分页响应
            PageResponse<List<Post>> pageResponse = 
                PageResponse.ofList(pageNo, pageSize, total, posts);
            
            return ResponseEntity.<PageResponse<List<Post>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取标签相关帖子成功")
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取标签相关帖子失败, tagId: {}", tagId, e);
            return ResponseEntity.<PageResponse<List<Post>>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取标签相关帖子失败: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 获取标签统计信息
     * 
     * <p>获取指定标签的使用统计信息，包括帖子数、是否推荐等
     * <p>公开接口，无需登录
     * 
     * @param tagId 标签ID
     * @return 标签统计信息，包含标签名称、描述、帖子数等
     */
    @GetMapping("/{tagId}/stats")
    @Operation(summary = "获取标签统计信息", description = "获取标签的使用统计信息")
    @ApiOperationLog(description = "获取标签统计信息")
    public ResponseEntity<TagStatisticsResponse> getTagStats(
            @Parameter(description = "标签ID") @PathVariable Long tagId) {
        try {
            if (tagId == null) {
                return ResponseEntity.<TagStatisticsResponse>builder()
                        .code(ResponseCode.PARAM_ERROR.getCode())
                        .info("标签ID不能为空")
                        .build();
            }
            
            // 获取标签信息
            Tag Tag = tagService.getTagById(tagId);
            if (Tag == null) {
                return ResponseEntity.<TagStatisticsResponse>builder()
                        .code(ResponseCode.SYSTEM_ERROR.getCode())
                        .info("标签不存在")
                        .build();
            }
            
            // 统计标签使用次数（帖子数量）
            int postCount = postTagRepository.getPostsByTagId(tagId, 0, Integer.MAX_VALUE).size();
            
            // 构建统计响应
            TagStatisticsResponse stats = new TagStatisticsResponse(
                tagId,
                Tag.getName(),
                Tag.getDescription(),
                postCount,
                Tag.getIsRecommended() == 1
            );
            
            return ResponseEntity.<TagStatisticsResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取标签统计信息成功")
                    .data(stats)
                    .build();
        } catch (Exception e) {
            log.error("获取标签统计信息失败, tagId: {}", tagId, e);
            return ResponseEntity.<TagStatisticsResponse>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取标签统计信息失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 标签统计响应DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TagStatisticsResponse {
        private Long tagId;
        private String tagName;
        private String description;
        private Integer postCount;
        private Boolean isRecommended;
    }
}