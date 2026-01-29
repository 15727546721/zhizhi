package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.column.BatchSortRequest;
import cn.xu.model.dto.column.ColumnCreateDTO;
import cn.xu.model.dto.column.ColumnUpdateDTO;
import cn.xu.model.dto.column.PostSortDTO;
import cn.xu.model.entity.Column;
import cn.xu.model.entity.ColumnPost;
import cn.xu.model.vo.column.ColumnDetailVO;
import cn.xu.model.vo.column.ColumnPostVO;
import cn.xu.model.vo.column.ColumnVO;
import cn.xu.service.column.ColumnApplicationService;
import cn.xu.service.column.ColumnPostService;
import cn.xu.service.column.ColumnService;
import cn.xu.service.column.ColumnSubscriptionService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 专栏控制器
 */
@Slf4j
@Tag(name = "专栏接口", description = "专栏管理API")
@RestController
@RequestMapping("/api/columns")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;
    private final ColumnPostService columnPostService;
    private final ColumnSubscriptionService subscriptionService;
    private final ColumnApplicationService applicationService;

    // ==================== 专栏CRUD ====================

    @Operation(summary = "创建专栏")
    @PostMapping
    @ApiOperationLog(description = "创建专栏")
    public ResponseEntity<ColumnVO> createColumn(@Valid @RequestBody ColumnCreateDTO request) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            Column column = columnService.createColumn(
                    userId, 
                    request.getName(), 
                    request.getDescription(), 
                    request.getCoverUrl(), 
                    request.getStatus());
            
            // 转换为VO
            ColumnVO vo = new ColumnVO();
            vo.setId(column.getId());
            vo.setUserId(column.getUserId());
            vo.setName(column.getName());
            vo.setDescription(column.getDescription());
            vo.setCoverUrl(column.getCoverUrl());
            vo.setStatus(column.getStatus());
            vo.setPostCount(column.getPostCount());
            vo.setSubscribeCount(column.getSubscribeCount());
            vo.setLastPostTime(column.getLastPostTime());
            vo.setCreateTime(column.getCreateTime());
            
            return ResponseEntity.<ColumnVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(vo)
                    .info("创建成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<ColumnVO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "更新专栏")
    @PutMapping("/{columnId}")
    @ApiOperationLog(description = "更新专栏")
    public ResponseEntity<Void> updateColumn(
            @Parameter(description = "专栏ID") @PathVariable Long columnId,
            @Valid @RequestBody ColumnUpdateDTO request) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            columnService.updateColumn(
                    userId, 
                    columnId, 
                    request.getName(), 
                    request.getDescription(), 
                    request.getCoverUrl(), 
                    request.getStatus());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("更新成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "删除专栏")
    @DeleteMapping("/{columnId}")
    @ApiOperationLog(description = "删除专栏")
    public ResponseEntity<Void> deleteColumn(
            @Parameter(description = "专栏ID") @PathVariable Long columnId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            columnService.deleteColumn(userId, columnId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "获取专栏详情")
    @GetMapping("/{columnId}")
    public ResponseEntity<ColumnDetailVO> getColumnDetail(
            @Parameter(description = "专栏ID") @PathVariable Long columnId) {
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录用户
        }
        
        ColumnDetailVO detail = applicationService.getColumnDetail(columnId, userId);
        if (detail == null) {
            return ResponseEntity.<ColumnDetailVO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("专栏不存在或无权访问")
                    .build();
        }
        
        return ResponseEntity.<ColumnDetailVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(detail)
                .build();
    }

    @Operation(summary = "获取用户的专栏列表")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ColumnVO>> getUserColumns(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        Long currentUserId = null;
        try {
            currentUserId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录用户
        }
        
        List<ColumnVO> columns = applicationService.getUserProfileColumns(userId, currentUserId);
        return ResponseEntity.<List<ColumnVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(columns)
                .build();
    }

    // ==================== 专栏文章管理 ====================

    @Operation(summary = "添加文章到专栏")
    @PostMapping("/{columnId}/posts/{postId}")
    @ApiOperationLog(description = "添加文章到专栏")
    public ResponseEntity<Void> addPostToColumn(
            @Parameter(description = "专栏ID") @PathVariable Long columnId,
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "排序值") @RequestParam(required = false) Integer sort) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            columnPostService.addPostToColumn(userId, columnId, postId, sort);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("添加成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "从专栏移除文章")
    @DeleteMapping("/{columnId}/posts/{postId}")
    @ApiOperationLog(description = "从专栏移除文章")
    public ResponseEntity<Void> removePostFromColumn(
            @Parameter(description = "专栏ID") @PathVariable Long columnId,
            @Parameter(description = "帖子ID") @PathVariable Long postId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            columnPostService.removePostFromColumn(userId, columnId, postId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("移除成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "批量调整文章顺序")
    @PutMapping("/{columnId}/posts/sort")
    @ApiOperationLog(description = "批量调整文章顺序")
    public ResponseEntity<Void> batchUpdateSort(
            @Parameter(description = "专栏ID") @PathVariable Long columnId,
            @Valid @RequestBody BatchSortRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            // 转换为ColumnPost列表
            List<ColumnPost> sortList = request.getSortList().stream()
                    .map(dto -> {
                        ColumnPost cp = new ColumnPost();
                        cp.setPostId(dto.getPostId());
                        cp.setSort(dto.getSort());
                        return cp;
                    })
                    .collect(Collectors.toList());
            
            columnPostService.batchUpdateSort(userId, columnId, sortList);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("排序成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "获取专栏的文章列表")
    @GetMapping("/{columnId}/posts")
    public ResponseEntity<PageResponse<List<ColumnPostVO>>> getColumnPosts(
            @Parameter(description = "专栏ID") @PathVariable Long columnId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        
        List<ColumnPostVO> posts = applicationService.getColumnPostsWithDetails(columnId, page, size);
        int total = columnPostService.getColumnPosts(columnId, 1, Integer.MAX_VALUE).size();
        
        return ResponseEntity.<PageResponse<List<ColumnPostVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(PageResponse.ofList(page, size, (long) total, posts))
                .build();
    }

    // ==================== 专栏订阅 ====================

    @Operation(summary = "订阅专栏")
    @PostMapping("/{columnId}/subscribe")
    @ApiOperationLog(description = "订阅专栏")
    public ResponseEntity<Void> subscribeColumn(
            @Parameter(description = "专栏ID") @PathVariable Long columnId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            subscriptionService.subscribe(userId, columnId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("订阅成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "取消订阅专栏")
    @DeleteMapping("/{columnId}/subscribe")
    @ApiOperationLog(description = "取消订阅专栏")
    public ResponseEntity<Void> unsubscribeColumn(
            @Parameter(description = "专栏ID") @PathVariable Long columnId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            subscriptionService.unsubscribe(userId, columnId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("取消订阅成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "获取用户订阅的专栏列表")
    @GetMapping("/subscriptions")
    @ApiOperationLog(description = "获取用户订阅的专栏列表")
    public ResponseEntity<PageResponse<List<ColumnVO>>> getUserSubscriptions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        PageResponse<List<ColumnVO>> result = applicationService.getUserSubscriptions(userId, page, size);
        
        return ResponseEntity.<PageResponse<List<ColumnVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    // ==================== 专栏广场 ====================

    @Operation(summary = "专栏广场")
    @GetMapping("/square")
    public ResponseEntity<PageResponse<List<ColumnVO>>> getColumnSquare(
            @Parameter(description = "排序类型: latest-最新, subscribe-订阅数") 
            @RequestParam(defaultValue = "latest") String sortType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录用户
        }
        
        PageResponse<List<ColumnVO>> result = applicationService.getHotColumns(sortType, page, size);
        
        return ResponseEntity.<PageResponse<List<ColumnVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    @Operation(summary = "搜索专栏")
    @GetMapping("/search")
    public ResponseEntity<PageResponse<List<ColumnVO>>> searchColumns(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size) {
        
        PageResponse<List<ColumnVO>> result = applicationService.searchColumns(keyword, page, size);
        
        return ResponseEntity.<PageResponse<List<ColumnVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    @Operation(summary = "推荐专栏")
    @GetMapping("/recommended")
    public ResponseEntity<List<ColumnVO>> getRecommendedColumns(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        
        List<ColumnVO> columns = applicationService.getRecommendedColumns(limit);
        
        return ResponseEntity.<List<ColumnVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(columns)
                .build();
    }

    @Operation(summary = "获取文章所属的专栏列表")
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<ColumnVO>> getPostColumns(
            @Parameter(description = "文章ID") @PathVariable Long postId) {
        
        Long userId = null;
        try {
            userId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录用户
        }
        
        List<ColumnVO> columns = applicationService.getPostColumns(postId, userId);
        
        return ResponseEntity.<List<ColumnVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(columns)
                .build();
    }

    @Operation(summary = "获取文章在专栏中的导航信息")
    @GetMapping("/{columnId}/posts/{postId}/navigation")
    public ResponseEntity<cn.xu.model.vo.column.ColumnPostNavigationVO> getPostNavigation(
            @Parameter(description = "专栏ID") @PathVariable Long columnId,
            @Parameter(description = "文章ID") @PathVariable Long postId) {
        
        cn.xu.model.vo.column.ColumnPostNavigationVO navigation = 
            applicationService.getPostNavigation(columnId, postId);
        
        return ResponseEntity.<cn.xu.model.vo.column.ColumnPostNavigationVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(navigation)
                .build();
    }
}
