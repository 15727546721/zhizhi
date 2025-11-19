package cn.xu.api.web.controller;

import cn.xu.common.ResponseCode;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.persistent.read.elastic.repository.PostElasticRepository;
import cn.xu.infrastructure.persistent.read.elastic.service.PostElasticService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Elasticsearch索引管理接口
 * 提供索引重新构建、状态查询等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/elasticsearch")
@Tag(name = "Elasticsearch索引管理", description = "Elasticsearch索引管理接口")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchIndexController {

    private final PostElasticService postElasticService;
    private final IPostRepository postRepository;
    private final PostElasticRepository postElasticRepository;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 重新索引所有帖子
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
                List<PostEntity> posts = postRepository.findAll(currentOffset, batchSize);
                
                if (posts == null || posts.isEmpty()) {
                    break;
                }
                
                // 索引每批帖子（只索引已发布的帖子）
                for (PostEntity post : posts) {
                    try {
                        // 只索引已发布的帖子（status = 1）
                        if (post.getStatusCode() == 1) {
                            postElasticService.indexPost(post);
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
     */
    @PostMapping("/reindex/{postId}")
    @Operation(summary = "重新索引单个帖子", description = "将指定ID的帖子重新索引到Elasticsearch")
    public ResponseEntity<ReindexResponse> reindexPost(
            @Parameter(description = "帖子ID") @PathVariable Long postId) {
        try {
            java.util.Optional<PostAggregate> postAggregateOpt = postRepository.findById(postId);
            if (!postAggregateOpt.isPresent()) {
                ReindexResponse response = new ReindexResponse();
                response.setMessage("帖子不存在: postId=" + postId);
                return ResponseEntity.<ReindexResponse>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("帖子不存在: postId=" + postId)
                        .data(response)
                        .build();
            }
            
            PostEntity post = postAggregateOpt.get().getPostEntity();
            postElasticService.indexPost(post);
            
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
     */
    @GetMapping("/status")
    @Operation(summary = "获取索引状态", description = "获取Elasticsearch索引的状态信息")
    public ResponseEntity<IndexStatusResponse> getIndexStatus() {
        try {
            // 获取ES索引中的帖子数量
            long esCount = postElasticRepository.count();
            
            // 获取MySQL中已发布的帖子数量
            // 注意：这里使用findAllPublished()方法，它只返回已发布的帖子
            long mysqlCount = 0;
            try {
                List<PostEntity> publishedPosts = postRepository.findAllPublished();
                mysqlCount = publishedPosts != null ? publishedPosts.size() : 0;
            } catch (Exception e) {
                log.warn("获取MySQL已发布帖子数量失败", e);
                // 如果方法不存在或失败，尝试使用countAll作为近似值
                try {
                    mysqlCount = postRepository.countAll();
                } catch (Exception ex) {
                    log.warn("获取MySQL帖子总数也失败", ex);
                }
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
                    java.util.Optional<PostAggregate> postAggregateOpt = postRepository.findById(postId);
                    if (postAggregateOpt.isPresent()) {
                        PostEntity post = postAggregateOpt.get().getPostEntity();
                        if (post != null && post.getStatusCode() == 1) {
                            boolean success = postElasticService.indexPostWithRetry(post);
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

    @GetMapping("/test-search")
    @Operation(summary = "测试ES搜索", description = "测试ES搜索功能，返回查询详情")
    public ResponseEntity<TestSearchResponse> testSearch(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量，默认10") @RequestParam(defaultValue = "10") int size) {
        try {
            TestSearchResponse response = new TestSearchResponse();
            response.setKeyword(keyword);
            
            // 1. 检查索引总数
            long totalCount = postElasticRepository.count();
            response.setTotalCount(totalCount);
            
            // 2. 尝试简单查询
            try {
                org.springframework.data.domain.Pageable pageable = 
                    org.springframework.data.domain.PageRequest.of(0, size);
                org.springframework.data.domain.Page<cn.xu.infrastructure.persistent.read.elastic.model.PostIndex> page = 
                    postElasticRepository.searchByTitleAndDescription(keyword, pageable);
                response.setSearchResultCount(page.getTotalElements());
                response.setSearchSuccess(true);
                response.setSearchMessage("搜索成功");
                
                // 获取前几条结果的标题
                List<String> sampleTitles = page.getContent().stream()
                    .limit(5)
                    .map(cn.xu.infrastructure.persistent.read.elastic.model.PostIndex::getTitle)
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
                org.springframework.data.domain.Page<cn.xu.infrastructure.persistent.read.elastic.model.PostIndex> allPage = 
                    postElasticRepository.findAll(pageable);
                List<String> allTitles = allPage.getContent().stream()
                    .map(cn.xu.infrastructure.persistent.read.elastic.model.PostIndex::getTitle)
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

