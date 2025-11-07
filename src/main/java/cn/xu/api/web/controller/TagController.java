package cn.xu.api.web.controller;

import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.post.model.entity.TagEntity;
import cn.xu.domain.post.service.IPostTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tag")
@Tag(name = "帖子标签接口", description = "帖子标签相关接口")
public class TagController {

    @Resource
    private IPostTagService tagService;

    @GetMapping("/list")
    @Operation(summary = "获取标签列表", description = "获取所有可用的标签列表")
    @ApiOperationLog(description = "获取标签列表")
    public ResponseEntity<List<TagEntity>> getTagList() {
        try {
            List<TagEntity> tagEntityList = tagService.getTagList();
            return ResponseEntity.<List<TagEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询标签列表成功")
                    .data(tagEntityList)
                    .build();
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return ResponseEntity.<List<TagEntity>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取标签列表失败")
                    .build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索标签", description = "根据关键词搜索标签")
    @ApiOperationLog(description = "搜索标签")
    public ResponseEntity<List<TagEntity>> searchTags(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        try {
            List<TagEntity> tagEntityList = tagService.searchTags(keyword);
            return ResponseEntity.<List<TagEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索标签成功")
                    .data(tagEntityList)
                    .build();
        } catch (Exception e) {
            log.error("搜索标签失败", e);
            return ResponseEntity.<List<TagEntity>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("搜索标签失败")
                    .build();
        }
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门标签", description = "获取使用频率最高的标签，支持时间维度查询")
    @ApiOperationLog(description = "获取热门标签")
    public ResponseEntity<List<TagEntity>> getHotTags(
            @Parameter(description = "时间范围：today(今日)、week(本周)、month(本月)、all(全部)") 
            @RequestParam(required = false, defaultValue = "all") String timeRange,
            @Parameter(description = "返回数量限制") 
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<TagEntity> tagEntityList = tagService.getHotTagsByTimeRange(timeRange, limit);
            // 确保不返回null
            if (tagEntityList == null) {
                tagEntityList = new java.util.ArrayList<>();
            }
            log.debug("获取热门标签成功: timeRange={}, limit={}, count={}", timeRange, limit, tagEntityList.size());
            return ResponseEntity.<List<TagEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取热门标签成功")
                    .data(tagEntityList)
                    .build();
        } catch (Exception e) {
            log.error("获取热门标签失败, timeRange: {}, limit: {}", timeRange, limit, e);
            return ResponseEntity.<List<TagEntity>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取热门标签失败: " + e.getMessage())
                    .data(new java.util.ArrayList<>())
                    .build();
        }
    }

    @GetMapping("/{tagId}/posts")
    @Operation(summary = "获取标签相关帖子", description = "获取使用指定标签的帖子列表")
    @ApiOperationLog(description = "获取标签相关帖子")
    public ResponseEntity<List<Object>> getTagPosts(
            @Parameter(description = "标签ID") @PathVariable Long tagId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            // 这里需要实现获取标签相关帖子的逻辑
            // List<Object> posts = tagService.getTagPosts(tagId, pageNo, pageSize);
            return ResponseEntity.<List<Object>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取标签相关帖子成功")
                    .data(null) // 暂时返回null，需要实现具体逻辑
                    .build();
        } catch (Exception e) {
            log.error("获取标签相关帖子失败", e);
            return ResponseEntity.<List<Object>>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取标签相关帖子失败")
                    .build();
        }
    }

    @GetMapping("/{tagId}/stats")
    @Operation(summary = "获取标签统计信息", description = "获取标签的使用统计信息")
    @ApiOperationLog(description = "获取标签统计信息")
    public ResponseEntity<Object> getTagStats(
            @Parameter(description = "标签ID") @PathVariable Long tagId) {
        try {
            // 这里需要实现获取标签统计信息的逻辑
            // Object stats = tagService.getTagStats(tagId);
            return ResponseEntity.<Object>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("获取标签统计信息成功")
                    .data(null) // 暂时返回null，需要实现具体逻辑
                    .build();
        } catch (Exception e) {
            log.error("获取标签统计信息失败", e);
            return ResponseEntity.<Object>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取标签统计信息失败")
                    .build();
        }
    }
}