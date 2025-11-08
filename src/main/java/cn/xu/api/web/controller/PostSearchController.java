package cn.xu.api.web.controller;

import cn.xu.application.service.SearchApplicationService;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.common.ResponseCode;
import cn.xu.domain.search.service.ISearchStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子搜索相关API控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/post/search")
@RequiredArgsConstructor
public class PostSearchController {

    private final SearchApplicationService searchApplicationService;

    @GetMapping("/suggestions")
    @Operation(summary = "获取搜索建议")
    @ApiOperationLog(description = "获取搜索建议")
    public ResponseEntity<List<String>> getSearchSuggestions(
            @Parameter(description = "搜索关键词前缀") @RequestParam(required = false) String keyword,
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<String> suggestions = searchApplicationService.getSearchSuggestions(keyword, limit);
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

    @GetMapping("/hot-keywords")
    @Operation(summary = "获取热门搜索词")
    @ApiOperationLog(description = "获取热门搜索词")
    public ResponseEntity<List<String>> getHotKeywords(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int limit) {
        try {
            int safeLimit = Math.max(1, Math.min(limit, 100));
            List<String> result = searchApplicationService.getHotKeywords(safeLimit);
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

    @GetMapping("/statistics")
    @Operation(summary = "获取搜索统计信息")
    @ApiOperationLog(description = "获取搜索统计信息")
    public ResponseEntity<ISearchStatisticsService.SearchStatistics> getSearchStatistics(
            @Parameter(description = "日期（格式：yyyy-MM-dd）") 
            @RequestParam(required = false) String date) {
        try {
            ISearchStatisticsService.SearchStatistics statistics = 
                    searchApplicationService.getSearchStatistics(date);
            return ResponseEntity.<ISearchStatisticsService.SearchStatistics>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(statistics)
                    .build();
        } catch (Exception e) {
            log.error("获取搜索统计失败: date={}", date, e);
            return ResponseEntity.<ISearchStatisticsService.SearchStatistics>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取搜索统计失败")
                    .build();
        }
    }

    @GetMapping("/hot-keywords/detailed")
    @Operation(summary = "获取热门搜索词详情（带搜索次数）")
    @ApiOperationLog(description = "获取热门搜索词详情")
    public ResponseEntity<List<ISearchStatisticsService.HotKeyword>> getHotKeywordsDetailed(
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ISearchStatisticsService.HotKeyword> result = 
                    searchApplicationService.getHotKeywordsDetailed(limit);
            
            return ResponseEntity.<List<ISearchStatisticsService.HotKeyword>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.error("获取热门搜索词详情失败", e);
            return ResponseEntity.<List<ISearchStatisticsService.HotKeyword>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取热门搜索词详情失败")
                    .data(new ArrayList<>())
                    .build();
        }
    }
}

