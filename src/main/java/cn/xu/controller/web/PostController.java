package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.request.CursorPageRequest;
import cn.xu.common.request.MyPostsRequest;
import cn.xu.common.request.PageRequest;
import cn.xu.common.request.UserPostsRequest;
import cn.xu.common.response.CursorPageResponse;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.post.DraftRequest;
import cn.xu.model.dto.post.PostPageQueryRequest;
import cn.xu.model.dto.post.PublishOrDraftPostRequest;
import cn.xu.model.entity.Like;
import cn.xu.model.vo.post.PostDetailVO;
import cn.xu.model.vo.post.PostListVO;
import cn.xu.model.vo.post.PostSearchResponseVO;
import cn.xu.service.like.LikeService;
import cn.xu.service.post.PostApplicationService;
import cn.xu.service.post.PostCommandService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子控制器
 * <p>
 * 职责：帖子内容管理、交互操作
 * <ul>
 *   <li>内容：创建、发布、编辑、删除、草稿</li>
 *   <li>查询：列表、详情、搜索、排行榜</li>
 *   <li>交互：点赞、收藏、分享、浏览</li>
 * </ul>
 */
@RequestMapping("/api/post")
@RestController
@Tag(name = "帖子接口", description = "帖子相关API")
@Slf4j
@RequiredArgsConstructor
public class PostController {

    private final PostApplicationService postApplicationService;
    private final PostCommandService postCommandService;
    private final LikeService likeService;

    // ==================== 游标分页接口（推荐使用） ====================

    /**
     * 游标分页获取帖子列表（性能优化版）
     * <p>
     * 相比传统 OFFSET 分页的优势：
     * <ul>
     *   <li>性能稳定：无论翻到第几页，查询性能一致</li>
     *   <li>数据一致：避免翻页时数据重复或遗漏</li>
     *   <li>适合无限滚动场景</li>
     * </ul>
     */
    @PostMapping("/cursor")
    @ApiOperationLog(description = "游标分页获取帖子列表")
    @Operation(summary = "游标分页获取帖子列表（推荐）")
    public ResponseEntity<CursorPageResponse<PostListVO>> getPostsByCursor(@RequestBody CursorPageRequest request) {
        try {
            CursorPageResponse<PostListVO> response = postApplicationService.getPostsByCursor(request);
            return ResponseEntity.<CursorPageResponse<PostListVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(response)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<CursorPageResponse<PostListVO>>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("游标分页获取帖子列表失败", e);
            return ResponseEntity.<CursorPageResponse<PostListVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子列表失败")
                    .build();
        }
    }

    // ==================== 传统分页接口 ====================

    /**
     * 分页获取帖子列表（支持多种排序，公开接口）
     */
    @PostMapping("/page")
    @ApiOperationLog(description = "分页获取帖子列表（支持排序）")
    @Operation(summary = "分页获取帖子列表（支持排序）")
    public ResponseEntity<PageResponse<List<PostListVO>>> getPostByPage(@RequestBody PostPageQueryRequest request) {
        try {
            PageResponse<List<PostListVO>> pageResponse = postApplicationService.getPostsByPage(
                    request.getPageNo(), request.getPageSize());
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("分页获取帖子列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子列表失败")
                    .build();
        }
    }

    /**
     * 搜索帖子（支持时间范围、排序方式筛选）
     */
    @GetMapping("/search")
    @Operation(summary = "搜索帖子")
    @ApiOperationLog(description = "搜索帖子")
    public ResponseEntity<PageResponse<List<PostSearchResponseVO>>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "帖子类型筛选") @RequestParam(required = false) String[] types,
            @Parameter(description = "发布时间范围筛选") @RequestParam(required = false, defaultValue = "all") String timeRange,
            @Parameter(description = "排序方式") @RequestParam(required = false, defaultValue = "time") String sortOption,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            PageResponse<List<PostSearchResponseVO>> pageResponse = postApplicationService.searchPosts(
                    keyword, types, timeRange, sortOption, page, size);
            return ResponseEntity.<PageResponse<List<PostSearchResponseVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("搜索参数错误: keyword={}, error={}", keyword, e.getMessage());
            return ResponseEntity.<PageResponse<List<PostSearchResponseVO>>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("搜索帖子失败: keyword={}", keyword, e);
            return ResponseEntity.<PageResponse<List<PostSearchResponseVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取帖子详情（含作者、标签、统计、交互状态）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情")
    @ApiOperationLog(description = "获取帖子详情")
    public ResponseEntity<PostDetailVO> getPostDetail(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id,
            HttpServletRequest request) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserIdOptional().orElse(null);
            String clientIp = getClientIp(request);
            PostDetailVO detail = postApplicationService.getPostDetail(id, currentUserId, clientIp);
            return ResponseEntity.<PostDetailVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(detail)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<PostDetailVO>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("获取帖子详情失败: postId={}", id, e);
            return ResponseEntity.<PostDetailVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子详情失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取文章所属的专栏列表
     */
    @GetMapping("/{id}/columns")
    @Operation(summary = "获取文章所属的专栏列表")
    @ApiOperationLog(description = "获取文章所属的专栏列表")
    public ResponseEntity<List<cn.xu.model.vo.column.ColumnVO>> getPostColumns(
            @Parameter(description = "帖子ID") @PathVariable("id") Long postId) {
        try {
            Long currentUserId = LoginUserUtil.getLoginUserIdOptional().orElse(null);
            List<cn.xu.model.vo.column.ColumnVO> columns = postApplicationService.getPostColumns(postId, currentUserId);
            return ResponseEntity.<List<cn.xu.model.vo.column.ColumnVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(columns)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<List<cn.xu.model.vo.column.ColumnVO>>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("获取文章专栏列表失败: postId={}", postId, e);
            return ResponseEntity.<List<cn.xu.model.vo.column.ColumnVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取文章专栏列表失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取我的帖子列表
     */
    @PostMapping("/my")
    @Operation(summary = "获取我的帖子列表")
    @SaCheckLogin
    @ApiOperationLog(description = "获取我的帖子列表")
    public ResponseEntity<PageResponse<List<PostListVO>>> getMyPosts(@RequestBody MyPostsRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            PageResponse<List<PostListVO>> pageResponse = postApplicationService.getMyPosts(
                    userId, request.getPageNo(), request.getPageSize(), 
                    request.getStatus(), request.getKeyword());
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("获取我的帖子列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子列表失败")
                    .build();
        }
    }

    /**
     * 获取指定用户的帖子列表（公开接口）
     */
    @PostMapping("/user/{userId}")
    @Operation(summary = "获取指定用户的帖子列表")
    @ApiOperationLog(description = "获取指定用户的帖子列表")
    public ResponseEntity<PageResponse<List<PostListVO>>> getUserPosts(
            @PathVariable Long userId, @RequestBody UserPostsRequest request) {
        try {
            PageResponse<List<PostListVO>> pageResponse = postApplicationService.getUserPosts(
                    userId, request.getPageNo(), request.getPageSize(), request.getStatus());
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("获取用户帖子列表失败，userId: {}", userId, e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子列表失败")
                    .build();
        }
    }

    /**
     * 获取我的草稿列表
     */
    @PostMapping("/drafts")
    @Operation(summary = "获取我的草稿列表")
    @SaCheckLogin
    @ApiOperationLog(description = "获取我的草稿列表")
    public ResponseEntity<PageResponse<List<PostListVO>>> getMyDrafts(@RequestBody PageRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            PageResponse<List<PostListVO>> pageResponse = postApplicationService.getMyDrafts(
                    userId, request.getPageNo(), request.getPageSize());
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("获取草稿列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取草稿列表失败")
                    .build();
        }
    }

    /**
     * 获取收藏排行榜
     */
    @GetMapping("/favorite/ranking")
    @Operation(summary = "获取收藏排行榜")
    @ApiOperationLog(description = "获取收藏排行榜")
    public ResponseEntity<List<PostListVO>> getFavoriteRanking(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<PostListVO> result = postApplicationService.getFavoriteRanking(limit);
            return ResponseEntity.<List<PostListVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.error("获取收藏排行榜失败", e);
            return ResponseEntity.<List<PostListVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取收藏排行榜失败")
                    .build();
        }
    }

    // ==================== 写操作接口 ====================

    /**
     * 创建帖子（支持直接发布或保存为草稿）
     */
    @PostMapping("/create")
    @Operation(summary = "创建帖子")
    @ApiOperationLog(description = "创建帖子")
    public ResponseEntity<Long> createPost(@RequestBody PublishOrDraftPostRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            Long postId = postApplicationService.createPost(
                    userId, request.getTitle(), request.getContent(), request.getDescription(),
                    request.getCoverUrl(), request.getTagIds(), request.getStatus());
            
            String message = "DRAFT".equals(request.getStatus()) ? "草稿保存成功" : "帖子发布成功";
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(postId)
                    .info(message)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Long>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("创建帖子异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("帖子创建失败")
                    .build();
        }
    }

    /**
     * 发布/更新帖子（草稿发布或已发布帖子更新）
     */
    @PostMapping("/publish")
    @Operation(summary = "发布帖子（更新帖子）")
    @SaCheckLogin
    @ApiOperationLog(description = "发布帖子（更新帖子）")
    public ResponseEntity<Long> publishPost(@RequestBody PublishOrDraftPostRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            Long postId = postApplicationService.publishOrUpdatePost(
                    request.getId(), userId, request.getTitle(), request.getContent(),
                    request.getDescription(), request.getCoverUrl(), request.getTagIds(), 
                    request.getStatus(), request.getColumnIds());
            
            String message = "DRAFT".equals(request.getStatus()) ? "草稿保存成功" : "帖子发布成功";
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(postId)
                    .info(message)
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Long>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("发布帖子异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("帖子发布失败")
                    .build();
        }
    }

    /**
     * 保存帖子草稿（支持新建和更新）
     */
    @PostMapping("/saveDraft")
    @Operation(summary = "保存帖子草稿")
    @SaCheckLogin
    @ApiOperationLog(description = "保存帖子草稿")
    public ResponseEntity<Long> saveDraft(@RequestBody DraftRequest draftRequest) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            Long postId = postApplicationService.saveDraft(
                    userId, draftRequest.getId(), draftRequest.getTitle(), draftRequest.getContent(),
                    draftRequest.getDescription(), draftRequest.getCoverUrl(), draftRequest.getTagIds());
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(postId)
                    .info("草稿保存成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Long>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("保存草稿失败")
                    .build();
        }
    }

    /**
     * 删除草稿（仅能删除自己的草稿）
     */
    @PostMapping("/draft/{id}/delete")
    @Operation(summary = "删除草稿")
    @SaCheckLogin
    @ApiOperationLog(description = "删除草稿")
    public ResponseEntity<?> deleteDraft(@PathVariable Long id) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            postApplicationService.deleteDraft(id, userId);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("草稿删除成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("删除草稿失败，id={}", id, e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除草稿失败")
                    .build();
        }
    }

    /**
     * 删除帖子（仅能删除自己的帖子）
     */
    @PostMapping("/{id}/delete")
    @Operation(summary = "删除帖子")
    @SaCheckLogin
    @ApiOperationLog(description = "删除帖子")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            postApplicationService.deletePost(id, userId);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("帖子删除成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("删除帖子失败，id={}", id, e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除帖子失败")
                    .build();
        }
    }

    // ==================== 交互接口 ====================

    /**
     * 增加帖子浏览数
     */
    @GetMapping("/view")
    @Operation(summary = "增加帖子浏览数")
    @ApiOperationLog(description = "增加帖子浏览数")
    public ResponseEntity<?> viewPost(@Parameter(description = "帖子ID") @RequestParam Long postId) {
        try {
            postCommandService.viewPost(postId, null, null);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("帖子阅读数+1成功")
                    .build();
        } catch (Exception e) {
            log.error("增加帖子浏览量失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("增加帖子浏览量失败")
                    .build();
        }
    }

    /**
     * 帖子点赞
     * @deprecated 请使用统一点赞接口 POST /api/likes/like，传入 targetId 和 type="POST"
     */
    @Deprecated
    @GetMapping("/like/{id}")
    @Operation(summary = "帖子点赞（已废弃，请使用 /api/likes/like）", deprecated = true)
    @ApiOperationLog(description = "帖子点赞")
    public ResponseEntity<?> likePost(@PathVariable("id") Long id) {
        Long userId = LoginUserUtil.getLoginUserId();
        likeService.like(userId, Like.LikeType.POST.getCode(), id);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子点赞成功")
                .build();
    }

    /**
     * 取消帖子点赞
     * @deprecated 请使用统一点赞接口 POST /api/likes/unlike，传入 targetId 和 type="POST"
     */
    @Deprecated
    @GetMapping("/unlike/{id}")
    @Operation(summary = "取消帖子点赞（已废弃，请使用 /api/likes/unlike）", deprecated = true)
    @ApiOperationLog(description = "取消帖子点赞")
    public ResponseEntity<?> unlikePost(@PathVariable("id") Long postId) {
        Long userId = LoginUserUtil.getLoginUserId();
        likeService.unlike(userId, Like.LikeType.POST.getCode(), postId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子取消点赞成功")
                .build();
    }

    // ==================== 私有方法 ====================

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
