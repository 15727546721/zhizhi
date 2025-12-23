package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.vo.search.AggregateSearchVO;
import cn.xu.service.search.AggregateSearchService;
import cn.xu.service.search.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聚合搜索控制器
 * 
 * <p>提供帖子、用户、标签的聚合搜索功能</p>
 * <p>使用异步编排并行搜索，提高搜索效率</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Tag(name = "聚合搜索接口", description = "帖子、用户、标签聚合搜索API")
public class AggregateSearchController {
    
    private final AggregateSearchService aggregateSearchService;
    private final SearchHistoryService searchHistoryService;
    
    /**
     * 聚合搜索
     * 
     * <p>同时搜索帖子、用户、标签，返回聚合结果
     *
     * @param keyword 搜索关键词（必填）
     * @param postLimit 帖子数量限制（默认10，最大50）
     * @param userLimit 用户数量限制（默认10，最大50）
     * @param tagLimit 标签数量限制（默认10，最大50）
     * @return 聚合搜索结果
     */
    @GetMapping("/aggregate")
    @Operation(summary = "聚合搜索", description = "同时搜索帖子、用户、标签")
    @ApiOperationLog(description = "聚合搜索")
    public ResponseEntity<AggregateSearchVO> aggregateSearch(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "帖子数量限制，默认10，最大50")
            @RequestParam(required = false, defaultValue = "10") Integer postLimit,
            @Parameter(description = "用户数量限制，默认10，最大50")
            @RequestParam(required = false, defaultValue = "10") Integer userLimit,
            @Parameter(description = "标签数量限制，默认10，最大50")
            @RequestParam(required = false, defaultValue = "10") Integer tagLimit) {
        
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.<AggregateSearchVO>builder()
                        .code(ResponseCode.PARAM_ERROR.getCode())
                        .info("搜索关键词不能为空")
                        .build();
            }
            
            // 记录搜索历史
            Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
            searchHistoryService.recordSearch(userId, keyword);
            
            AggregateSearchVO result = aggregateSearchService.search(
                    keyword, postLimit, userLimit, tagLimit);
            
            return ResponseEntity.<AggregateSearchVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索成功")
                    .data(result)
                    .build();
                    
        } catch (Exception e) {
            log.error("聚合搜索失败: keyword={}", keyword, e);
            return ResponseEntity.<AggregateSearchVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 仅搜索帖子
     * 
     * <p>只返回帖子搜索结果
     * <p>公开接口，无需登录
     * 
     * @param keyword 搜索关键词
     * @param limit 数量限制，默认20
     * @return 帖子搜索结果
     */
    @GetMapping("/posts")
    @Operation(summary = "搜索帖子", description = "仅搜索帖子")
    @ApiOperationLog(description = "搜索帖子")
    public ResponseEntity<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.PostSearchItem>> searchPosts(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "数量限制，默认20")
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        
        try {
            AggregateSearchVO result = aggregateSearchService.search(keyword, limit, 0, 0);
            
            return ResponseEntity.<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.PostSearchItem>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索成功")
                    .data(result.getPosts())
                    .build();
                    
        } catch (Exception e) {
            log.error("搜索帖子失败: keyword={}", keyword, e);
            return ResponseEntity.<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.PostSearchItem>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索失败")
                    .build();
        }
    }
    
    /**
     * 仅搜索用户
     * 
     * <p>只返回用户搜索结果
     * <p>公开接口，无需登录
     * 
     * @param keyword 搜索关键词
     * @param limit 数量限制，默认20
     * @return 用户搜索结果
     */
    @GetMapping("/users")
    @Operation(summary = "搜索用户", description = "仅搜索用户")
    @ApiOperationLog(description = "搜索用户")
    public ResponseEntity<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.UserSearchItem>> searchUsers(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "数量限制，默认20")
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        
        try {
            AggregateSearchVO result = aggregateSearchService.search(keyword, 0, limit, 0);
            
            return ResponseEntity.<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.UserSearchItem>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索成功")
                    .data(result.getUsers())
                    .build();
                    
        } catch (Exception e) {
            log.error("搜索用户失败: keyword={}", keyword, e);
            return ResponseEntity.<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.UserSearchItem>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索失败")
                    .build();
        }
    }
    
    /**
     * 仅搜索标签
     * 
     * <p>只返回标签搜索结果
     * <p>公开接口，无需登录
     * 
     * @param keyword 搜索关键词
     * @param limit 数量限制，默认20
     * @return 标签搜索结果
     */
    @GetMapping("/tags")
    @Operation(summary = "搜索标签", description = "仅搜索标签")
    @ApiOperationLog(description = "搜索标签")
    public ResponseEntity<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.TagSearchItem>> searchTags(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam String keyword,
            @Parameter(description = "数量限制，默认20")
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        
        try {
            AggregateSearchVO result = aggregateSearchService.search(keyword, 0, 0, limit);
            
            return ResponseEntity.<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.TagSearchItem>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索成功")
                    .data(result.getTags())
                    .build();
                    
        } catch (Exception e) {
            log.error("搜索标签失败: keyword={}", keyword, e);
            return ResponseEntity.<AggregateSearchVO.SearchResultGroup<AggregateSearchVO.TagSearchItem>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索失败")
                    .build();
        }
    }
    
    // ==================== 搜索历史与热词 ====================
    
    /**
     * 获取搜索历史
     * 
     * <p>返回当前登录用户的搜索历史（最近20条）
     * 
     * @return 搜索历史列表
     */
    @GetMapping("/history")
    @Operation(summary = "获取搜索历史", description = "获取当前用户的搜索历史")
    public ResponseEntity<List<String>> getSearchHistory() {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        List<String> history = searchHistoryService.getHistory(userId);
        return ResponseEntity.<List<String>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(history)
                .build();
    }
    
    /**
     * 删除单条搜索历史
     * 
     * @param keyword 要删除的关键词
     * @return 操作结果
     */
    @DeleteMapping("/history")
    @Operation(summary = "删除搜索历史", description = "删除指定的搜索历史")
    public ResponseEntity<Void> deleteSearchHistory(@RequestParam String keyword) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        searchHistoryService.deleteHistory(userId, keyword);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }
    
    /**
     * 清空搜索历史
     * 
     * @return 操作结果
     */
    @DeleteMapping("/history/clear")
    @Operation(summary = "清空搜索历史", description = "清空当前用户的所有搜索历史")
    public ResponseEntity<Void> clearSearchHistory() {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        searchHistoryService.clearHistory(userId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("清空成功")
                .build();
    }
    
    /**
     * 获取热门搜索词
     * 
     * <p>返回热门搜索词（Top10）
     * 
     * @return 热门搜索词列表
     */
    @GetMapping("/hot")
    @Operation(summary = "获取热门搜索", description = "获取热门搜索词")
    public ResponseEntity<List<String>> getHotWords() {
        List<String> hotWords = searchHistoryService.getHotWords();
        return ResponseEntity.<List<String>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(hotWords)
                .build();
    }
    
    /**
     * 获取搜索建议
     * 
     * <p>根据输入前缀匹配历史和热词
     * 
     * @param prefix 输入前缀
     * @return 搜索建议列表
     */
    @GetMapping("/suggest")
    @Operation(summary = "获取搜索建议", description = "根据输入获取搜索建议")
    public ResponseEntity<List<String>> getSuggestions(
            @Parameter(description = "输入前缀") @RequestParam String prefix) {
        Long userId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        List<String> suggestions = searchHistoryService.getSuggestions(userId, prefix);
        return ResponseEntity.<List<String>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(suggestions)
                .build();
    }
}