package cn.xu.repository.read.elastic.init;

import cn.xu.cache.core.RedisOperations;
import cn.xu.integration.search.strategy.ElasticsearchSearchStrategy;
import cn.xu.model.entity.Post;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.read.elastic.repository.PostElasticRepository;
import cn.xu.service.post.PostStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章数据ES索引初始化类
 * 主要在应用启动时同步ES索引，如果数据不一致或为空则触发ES索引初始化。
 * 配置项说明：
 * - app.elasticsearch.auto-init.enabled: 是否启用自动初始化（默认true）
 * - app.elasticsearch.auto-init.sync-threshold: 数据同步阈值，当MySQL数据与ES数据同步率低于该值时触发初始化（默认0.5，表示50%）
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@Order(100) // 在CommentDataInitializer之后执行
public class PostDataInitializer implements ApplicationRunner {

    private final PostStatisticsService postStatisticsService;
    private final PostMapper postMapper;
    private final ElasticsearchSearchStrategy esStrategy;
    private final PostElasticRepository postElasticRepository;

    @Autowired(required = false)
    private RedisOperations redisOps;

    @javax.annotation.PostConstruct
    public void onInit() {
        log.info("PostDataInitializer Bean已初始化，等待ApplicationRunner执行...");
    }

    @Value("${app.elasticsearch.auto-init.enabled:true}")
    private boolean autoInitEnabled;

    @Value("${app.elasticsearch.auto-init.sync-threshold:0.5}")
    private double syncThreshold;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== ES索引初始化开始 ===");
        log.info("自动初始化是否启用: enabled={}, 同步阈值={}", autoInitEnabled, syncThreshold);

        try {
            // 获取MySQL中已发布文章的数量
            long mysqlCount = 0;
            try {
                log.info("正在查询MySQL中已发布文章的数量...");
                mysqlCount = postStatisticsService.countPublished();
                log.info("MySQL中已发布文章数量: {}", mysqlCount);
            } catch (Exception e) {
                log.error("获取MySQL已发布文章数量失败，可能触发ES索引初始化，错误信息: {}", e.getMessage(), e);
                return;
            }

            // 获取ES中文章索引的数量
            long esCount = 0;
            try {
                log.info("正在查询ES中文章索引的数量...");
                esCount = postElasticRepository.count();
                log.info("ES中文章索引数量: {}", esCount);
            } catch (Exception e) {
                log.error("获取ES中文章索引数量失败，错误信息: {}", e.getMessage(), e);
                return;
            }

            // 计算同步率
            double syncRate = mysqlCount > 0 ? (double) esCount / mysqlCount : 0;

            log.info("MySQL中已发布文章数量: {}, ES中文章索引数量: {}, 同步率: {}%", mysqlCount, esCount, String.format("%.2f", syncRate * 100));

            // 判断是否需要初始化
            boolean needInit = false;
            String reason = "";

            if (mysqlCount > 0 && esCount == 0) {
                // 如果ES中没有索引数据，需初始化
                needInit = true;
                reason = "ES中没有文章索引数据";
            } else if (mysqlCount > 0 && syncRate < syncThreshold) {
                // 如果同步率低于阈值，需初始化
                needInit = true;
                reason = String.format("同步率低于阈值（%.2f%% < %.2f%%）", syncRate * 100, syncThreshold * 100);
            }

            // 如果需要初始化
            if (needInit) {
                if (autoInitEnabled) {
                    log.warn("ES索引数据不一致，正在初始化... 原因: {}", reason);
                    initializeIndex();
                } else {
                    log.warn("ES索引数据不一致，但未启用自动初始化。请手动触发初始化操作。");
                    log.warn("1. 调用接口: POST /api/admin/elasticsearch/reindex");
                    log.warn("2. 配置启用自动初始化: app.elasticsearch.auto-init.enabled=true");
                }
            } else {
                log.info("ES索引与MySQL数据一致，无需初始化。");
                log.info("=== ES索引初始化完成 ===");
            }
        } catch (Exception e) {
            log.error("ES索引初始化过程中发生异常，错误信息: {}", e.getMessage(), e);
            log.error("=== ES索引初始化结束 ===");
        }
    }

    /**
     * 初始化ES索引数据
     */
    private void initializeIndex() {
        try {
            log.info("开始初始化ES文章索引...");

            int batchSize = 100;
            int offset = 0;
            int totalIndexed = 0;
            int totalSkipped = 0;
            int totalFailed = 0;
            List<Long> failedPostIds = new java.util.ArrayList<>();

            while (true) {
                // 分页查询文章数据
                List<Post> posts = postMapper.findAllWithPagination(offset, batchSize);

                if (posts == null || posts.isEmpty()) {
                    break;
                }

                // 遍历并索引文章
                for (Post post : posts) {
                    try {
                        // 仅索引已发布的文章
                        if (Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
                            boolean success = esStrategy.indexPostWithRetry(post);
                            if (success) {
                                totalIndexed++;
                            } else {
                                log.warn("文章索引失败: postId={}", post.getId());
                                totalFailed++;
                                failedPostIds.add(post.getId());
                                // 将失败的任务记录到Redis中
                                recordFailedIndexTask(post.getId());
                            }
                        } else {
                            totalSkipped++;
                        }
                    } catch (Exception e) {
                        log.warn("处理文章索引时发生异常: postId={}", post.getId(), e);
                        totalFailed++;
                        failedPostIds.add(post.getId());
                        // 记录失败任务
                        recordFailedIndexTask(post.getId());
                    }
                }

                log.info("已处理 {} 条文章，跳过 {} 条，失败 {} 条，当前页: {} - {}",
                        totalIndexed, totalSkipped, totalFailed, offset, offset + posts.size());

                if (posts.size() < batchSize) {
                    break;
                }

                offset += batchSize;
            }

            log.info("ES文章索引初始化完成，总共处理文章: {}，跳过: {}，失败: {}",
                    totalIndexed, totalSkipped, totalFailed);

            // 如果有失败的任务，打印失败的文章ID，并提示手动重试
            if (!failedPostIds.isEmpty()) {
                log.warn("初始化过程中有 {} 条文章索引失败，具体失败ID: {}", failedPostIds.size(), failedPostIds.stream().limit(10).collect(java.util.stream.Collectors.toList()));
                log.warn("1. 调用接口: POST /api/admin/elasticsearch/reindex");
                log.warn("2. 调用接口: POST /api/admin/elasticsearch/reindex/failed");
            }

            log.info("=== ES索引初始化结束 ===");
        } catch (Exception e) {
            log.error("初始化ES文章索引时发生异常，错误信息: {}", e.getMessage(), e);
            log.error("=== ES索引初始化结束 ===");
        }
    }

    /**
     * 记录失败的索引任务
     */
    private void recordFailedIndexTask(Long postId) {
        if (redisOps == null || postId == null) {
            return;
        }
        try {
            String failedTasksKey = "es:index:failed:tasks";
            redisOps.sAdd(failedTasksKey, postId.toString());
            redisOps.expire(failedTasksKey, 24 * 3600);
            log.debug("记录失败的ES索引任务: postId={}", postId);
        } catch (Exception e) {
            log.warn("记录失败的ES索引任务时发生异常: postId={}", postId, e);
        }
    }
}
