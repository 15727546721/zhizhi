package cn.xu.controller.web;

import cn.xu.common.ResponseCode;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Post;
import cn.xu.service.post.PostQueryService;
import cn.xu.service.post.PostStatisticsService;
import cn.xu.service.search.ElasticsearchIndexManager;
import cn.xu.service.search.SearchStrategy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Elasticsearch索引管理控制器
 * 
 * <p>提供索引重新构建、状态查询、数据同步等功能接口</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/elasticsearch")
@Tag(name = "Elasticsearch索引管理", description = "Elasticsearch索引管理接口")
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchIndexController {

    // 依赖接口而非具体实现，符合依赖倒置原则（DIP）
    private final ElasticsearchIndexManager indexManager;
    private final SearchStrategy searchStrategy;
    private final PostQueryService postQueryService;
    private final PostStatisticsService postStatisticsService;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 构造器注入
     * 使用@Qualifier指定注入elasticsearchSearchStrategy而非mysqlSearchStrategy
     */
    public ElasticsearchIndexController(
            ElasticsearchIndexManager indexManager,
            @Qualifier("elasticsearchSearchStrategy") SearchStrategy searchStrategy,
            PostQueryService postQueryService,
            PostStatisticsService postStatisticsService) {
        this.indexManager = indexManager;
        this.searchStrategy = searchStrategy;
        this.postQueryService = postQueryService;
        this.postStatisticsService = postStatisticsService;
    }

    /**
     * 重新索引所有帖子
     * 
     * <p>将MySQL中的所有已发布帖子重新索引到Elasticsearch
     * <p>支持分批处理，适合大数据量场景
     * 
     * @param batchSize 批次大小，默认100
     * @param offset 起始偏移量，默认0
     * @return 索引结果统计
     */
    @PostMapping("/reindex")
    @Operation(summary = "重新索引所有帖子", description = "将MySQL中的所有已发布帖子重新索引到Elasticsearch")
    public ResponseEntity<ReindexResponse> reindexAllPosts(
            @Parameter(description = "批次大小，默认100") @RequestParam(defaultValue = "100") int batchSize,
            @Parameter(description = "起始偏移量，默认0") @RequestParam(defaultValue = "0") int offset) {
        try {
            log.info("开始重新索引所有帖子到Elasticsearch，batchSize={}, offset={}", batchSize, offset);
            
            int totalIndexed = 0;
            int totalSkipped = 0;
            int totalFailed = 0;
            int currentOffset = offset;
            
            while (true) {
                // 分批获取已发布的帖子
                List<Post> posts = postQueryService.getAll(currentOffset / batchSize + 1, batchSize);
                
                if (posts == null || posts.isEmpty()) {
                    break;
                }
                
                // 索引每批帖子（只索引已发布的帖子）
                for (Post post : posts) {
                    try {
                        // 只索引已发布的帖子
                        if (Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
                            indexManager.indexPost(post);
                            totalIndexed++;
                        } else {
                            totalSkipped++;
                        }
                    } catch (Exception e) {
                        log.warn("索引帖子失败: postId={}", post.getId(), e);
                        totalFailed++;
                    }
                }
                
                log.info("已索引 {} 个帖子，跳过 {} 个，失败 {} 个，当前批次: {} - {}", 
                        totalIndexed, totalSkipped, totalFailed, currentOffset, currentOffset + posts.size());
                
                if (posts.size() < batchSize) {
                    break;
                }
                
                currentOffset += batchSize;
            }
            
            ReindexResponse response = new ReindexResponse();
            response.setTotalIndexed(totalIndexed);
            response.setTotalSkipped(totalSkipped);
            response.setTotalFailed(totalFailed);
            response.setMessage("重新索引完成！");
            
            log.info("重新索引完成！共索引 {} 个帖子，跳过 {} 个，失败 {} 个", totalIndexed, totalSkipped, totalFailed);
            
            return ResponseEntity.<ReindexResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("重新索引完成！")
                    .data(response)
                    .build();
        } catch (Exception e) {
            log.error("重新索引失败", e);
            ReindexResponse response = new ReindexResponse();
            response.setMessage("重新索引失败: " + e.getMessage());
            return ResponseEntity.<ReindexResponse>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("重新索引失败: " + e.getMessage())
                    .data(response)
                    .build();
        }
    }

    /**
     * 重新索引单个帖子
     * 
     * <p>将指定ID的帖子重新索引到Elasticsearch
     * 
     * @param postId 帖子ID
     * @return 索引结果
     */
    @PostMapping("/reindex/{postId}")
    @Operation(summary = "重新索引单个帖子", description = "将指定ID的帖子重新索引到Elasticsearch")
    public ResponseEntity<ReindexResponse> reindexPost(
            @Parameter(description = "帖子ID") @PathVariable Long postId) {
        try {
            java.util.Optional<Post> postOpt = postQueryService.getById(postId);
            if (!postOpt.isPresent()) {
                ReindexResponse response = new ReindexResponse();
                response.setMessage("帖子不存在: postId=" + postId);
                return ResponseEntity.<ReindexResponse>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("帖子不存在: postId=" + postId)
                        .data(response)
                        .build();
            }
            
            Post post = postOpt.get();
            indexManager.indexPost(post);
            
            ReindexResponse response = new ReindexResponse();
            response.setTotalIndexed(1);
            response.setMessage("帖子索引成功: postId=" + postId);
            
            return ResponseEntity.<ReindexResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("帖子索引成功")
                    .data(response)
                    .build();
        } catch (Exception e) {
            log.error("重新索引帖子失败: postId={}", postId, e);
            ReindexResponse response = new ReindexResponse();
            response.setMessage("重新索引失败: " + e.getMessage());
            return ResponseEntity.<ReindexResponse>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("重新索引失败: " + e.getMessage())
                    .data(response)
                    .build();
        }
    }

    /**
     * 获取索引状态
     * 
     * <p>获取Elasticsearch索引的状态信息，包括ES和MySQL的数据量对比
     * 
     * @return 索引状态信息
     */
    @GetMapping("/status")
    @Operation(summary = "获取索引状态", description = "获取Elasticsearch索引的状态信息")
    public ResponseEntity<IndexStatusResponse> getIndexStatus() {
        try {
            // 获取ES索引中的帖子数量
            long esCount = indexManager.count();
            
            // 获取MySQL中已发布的帖子数量
            long mysqlCount = 0;
            try {
                mysqlCount = postStatisticsService.countAll();
            } catch (Exception e) {
                log.warn("获取MySQL帖子数量失败", e);
            }
            
            IndexStatusResponse response = new IndexStatusResponse();
            response.setEsPostCount(esCount);
            response.setMysqlPostCount(mysqlCount);
            response.setSyncRate(mysqlCount > 0 ? (double) esCount / mysqlCount * 100 : 0);
            response.setMessage("索引状态查询成功");
            
            return ResponseEntity.<IndexStatusResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("索引状态查询成功")
                    .data(response)
                    .build();
        } catch (Exception e) {
            log.error("获取索引状态失败", e);
            IndexStatusResponse response = new IndexStatusResponse();
            response.setMessage("获取索引状态失败: " + e.getMessage());
            return ResponseEntity.<IndexStatusResponse>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取索引状态失败: " + e.getMessage())
                    .data(response)
                    .build();
        }
    }

    @Data
    public static class ReindexResponse {
        private int totalIndexed;
        private int totalSkipped;
        private int totalFailed;
        private String message;
    }

    /**
     * 重试失败的索引任务
     * 
     * <p>从Redis获取失败任务列表并重试索引
     * 
     * @return 重试结果统计
     */
    @PostMapping("/reindex/failed")
    @Operation(summary = "重试失败的索引任务", description = "重试之前失败的ES索引任务")
    public ResponseEntity<ReindexResponse> reindexFailedTasks() {
        try {
            log.info("开始重试失败的ES索引任务...");
            
            // 从Redis获取失败的索引任务
            String failedTasksKey = "es:index:failed:tasks";
            Set<Object> failedPostIds = redisTemplate.opsForSet().members(failedTasksKey);
            
            if (failedPostIds == null || failedPostIds.isEmpty()) {
                ReindexResponse response = new ReindexResponse();
                response.setMessage("没有失败的索引任务");
                return ResponseEntity.<ReindexResponse>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .info("没有失败的索引任务")
                        .data(response)
                        .build();
            }
            
            int totalIndexed = 0;
            int totalFailed = 0;
            
            for (Object postIdObj : failedPostIds) {
                try {
                    Long postId = Long.parseLong(postIdObj.toString());
                    java.util.Optional<Post> postOpt = postQueryService.getById(postId);
                    if (postOpt.isPresent()) {
                        Post post = postOpt.get();
                        if (post != null && "PUBLISHED".equals(post.getStatus())) {
                            boolean success = indexManager.indexPostWithRetry(post);
                            if (success) {
                                totalIndexed++;
                                // 从失败列表中移除
                                redisTemplate.opsForSet().remove(failedTasksKey, postIdObj);
                            } else {
                                log.warn("重试索引帖子失败: postId={}", postId);
                                totalFailed++;
                            }
                        } else {
                            // 帖子未发布，从失败列表中移除
                            redisTemplate.opsForSet().remove(failedTasksKey, postIdObj);
                        }
                    } else {
                        // 帖子不存在，从失败列表中移除
                        redisTemplate.opsForSet().remove(failedTasksKey, postIdObj);
                    }
                } catch (Exception e) {
                    log.warn("处理失败的索引任务失败: postId={}", postIdObj, e);
                    totalFailed++;
                }
            }
            
            ReindexResponse response = new ReindexResponse();
            response.setTotalIndexed(totalIndexed);
            response.setTotalFailed(totalFailed);
            response.setMessage("重试完成，成功: " + totalIndexed + ", 失败: " + totalFailed);
            
            log.info("重试失败的ES索引任务完成！成功: {}, 失败: {}", totalIndexed, totalFailed);
            
            return ResponseEntity.<ReindexResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("重试完成")
                    .data(response)
                    .build();
        } catch (Exception e) {
            log.error("重试失败的ES索引任务失败", e);
            ReindexResponse response = new ReindexResponse();
            response.setMessage("重试失败: " + e.getMessage());
            return ResponseEntity.<ReindexResponse>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("重试失败: " + e.getMessage())
                    .data(response)
                    .build();
        }
    }

    /**
     * 测试ES搜索
     * 
     * <p>测试ES搜索功能，返回查询详情和示例结果
     * 
     * @param keyword 搜索关键词
     * @param size 返回数量，默认10
     * @return 搜索测试结果
     */
    @GetMapping("/test-search")
    @Operation(summary = "测试ES搜索", description = "测试ES搜索功能，返回查询详情")
    public ResponseEntity<TestSearchResponse> testSearch(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int size) {
        try {
            TestSearchResponse response = new TestSearchResponse();
            response.setKeyword(keyword);
            
            // 1. 检查索引总数
            long totalCount = indexManager.count();
            response.setTotalCount(totalCount);
            
            // 2. 尝试简单查询
            try {
                org.springframework.data.domain.Pageable pageable = 
                    org.springframework.data.domain.PageRequest.of(0, size);
                org.springframework.data.domain.Page<Post> page =
                    searchStrategy.search(keyword, pageable);
                response.setSearchResultCount(page.getTotalElements());
                response.setSearchSuccess(true);
                response.setSearchMessage("搜索成功");
                
                // 获取前几条结果的标题
                List<String> sampleTitles = page.getContent().stream()
                    .limit(5)
                    .map(Post::getTitle)
                    .collect(java.util.stream.Collectors.toList());
                response.setSampleTitles(sampleTitles);
            } catch (Exception e) {
                response.setSearchSuccess(false);
                response.setSearchMessage("搜索失败: " + e.getMessage());
                response.setSearchError(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            
            // 3. 尝试查询所有数据（不分页）
            try {
                org.springframework.data.domain.Pageable pageable = 
                    org.springframework.data.domain.PageRequest.of(0, Math.min(10, (int)totalCount));
                org.springframework.data.domain.Page<Post> allPage =
                    searchStrategy.search("", pageable);
                List<String> allTitles = allPage.getContent().stream()
                    .map(Post::getTitle)
                    .collect(java.util.stream.Collectors.toList());
                response.setAllSampleTitles(allTitles);
            } catch (Exception e) {
                response.setAllQueryError(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            
            return ResponseEntity.<TestSearchResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("测试搜索完成")
                    .data(response)
                    .build();
        } catch (Exception e) {
            log.error("测试搜索失败", e);
            TestSearchResponse response = new TestSearchResponse();
            response.setKeyword(keyword);
            response.setSearchSuccess(false);
            response.setSearchMessage("测试失败: " + e.getMessage());
            return ResponseEntity.<TestSearchResponse>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("测试搜索失败: " + e.getMessage())
                    .data(response)
                    .build();
        }
    }

    @Data
    public static class IndexStatusResponse {
        private long esPostCount;
        private long mysqlPostCount;
        private double syncRate; // 同步率（百分比）
        private String message;
    }
    
    @Data
    public static class TestSearchResponse {
        private String keyword;
        private long totalCount;
        private boolean searchSuccess;
        private String searchMessage;
        private String searchError;
        private long searchResultCount;
        private List<String> sampleTitles;
        private List<String> allSampleTitles;
        private String allQueryError;
    }
}