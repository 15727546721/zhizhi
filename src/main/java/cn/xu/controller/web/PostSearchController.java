import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.service.search.PostSearchService;
import cn.xu.service.search.SearchStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子搜索控制器
 * 
 * <p>提供帖子搜索建议、热门搜索词、搜索统计等功能接口
 * 
 *
 */
@Slf4j
@Tag(name = "帖子搜索接口", description = "帖子搜索相关API")
@RestController
@RequestMapping("/api/post/search")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostSearchService postSearchService;

    /**
     * 获取搜索建议
     * 
     * <p>根据关键词前缀获取搜索建议
     * <p>公开接口，无需登录
     * 
     * @param keyword 搜索关键词前缀
     * @param limit 返回数量，默认10
     * @return 搜索建议列表
     */
    @GetMapping("/suggestions")
    @Operation(summary = "获取搜索建议")
    @ApiOperationLog(description = "获取搜索建议")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @Parameter(description = "搜索关键词前缀") @RequestParam(required = false) String keyword,
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<String> suggestions = postSearchService.getSearchSuggestions(keyword, limit);
            return ResponseEntity.<List<String>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(suggestions)
                    .build();
        } catch (Exception e) {
            log.error("获取搜索建议失败: keyword={}", keyword, e);
            return ResponseEntity.<List<String>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取搜索建议失败")
                    .data(new ArrayList<>())
                    .build();
        }
    }

    /**
     * 获取热门搜索词
     * 
     * <p>获取最热门的搜索关键词列表
     * <p>公开接口，无需登录
     * 
     * @param limit 返回数量，默认10，最大100
     * @return 热门搜索词列表
     */
    @GetMapping("/hot-keywords")
    @Operation(summary = "获取热门搜索词")
    @ApiOperationLog(description = "获取热门搜索词")
    public ResponseEntity<List<String>> getHotKeywords(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int limit) {
        try {
            int safeLimit = Math.max(1, Math.min(limit, 100));
            List<String> result = postSearchService.getHotKeywords(safeLimit);
            if (result == null) {
                result = new ArrayList<>();
            }
            return ResponseEntity.<List<String>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.error("获取热门搜索词失败: limit={}", limit, e);
            return ResponseEntity.<List<String>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取热门搜索词失败")
                    .data(new ArrayList<>())
                    .build();
        }
    }

    /**
     * 获取搜索统计信息
     * 
     * <p>获取指定日期的搜索统计数据
     * <p>公开接口，无需登录
     * 
     * @param date 日期（格式：yyyy-MM-dd），默认今天
     * @return 搜索统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取搜索统计信息")
    @ApiOperationLog(description = "获取搜索统计信息")
    public ResponseEntity<SearchStatisticsService.SearchStatistics> getSearchStatistics(
            @Parameter(description = "日期（格式：yyyy-MM-dd）") 
            @RequestParam(required = false) String date) {
        try {
            SearchStatisticsService.SearchStatistics statistics = 
                    postSearchService.getSearchStatistics(date);
            return ResponseEntity.<SearchStatisticsService.SearchStatistics>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(statistics)
                    .build();
        } catch (Exception e) {
            log.error("获取搜索统计失败: date={}", date, e);
            return ResponseEntity.<SearchStatisticsService.SearchStatistics>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取搜索统计失败")
                    .build();
        }
    }

    /**
     * 获取热门搜索词详情
     * 
     * <p>获取热门搜索词及其搜索次数
     * <p>公开接口，无需登录
     * 
     * @param limit 返回数量，默认10
     * @return 热门搜索词详情列表
     */
    @GetMapping("/hot-keywords/detailed")
    @Operation(summary = "获取热门搜索词详情（带搜索次数）")
    @ApiOperationLog(description = "获取热门搜索词详情")
    public ResponseEntity<List<SearchStatisticsService.HotKeyword>> getHotKeywordsDetailed(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<SearchStatisticsService.HotKeyword> result = 
                    postSearchService.getHotKeywordsDetailed(limit);
            
            return ResponseEntity.<List<SearchStatisticsService.HotKeyword>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.error("获取热门搜索词详情失败", e);
            return ResponseEntity.<List<SearchStatisticsService.HotKeyword>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取热门搜索词详情失败")
                    .data(new ArrayList<>())
                    .build();
        }
    }
}