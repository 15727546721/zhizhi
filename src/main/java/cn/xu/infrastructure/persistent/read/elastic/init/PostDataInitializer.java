package cn.xu.infrastructure.persistent.read.elastic.init;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.persistent.read.elastic.repository.PostElasticRepository;
import cn.xu.infrastructure.persistent.read.elastic.service.PostElasticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 帖子数据ES索引初始化器
 * 在应用启动时检查ES索引状态，如果数据不一致则自动初始化
 * 
 * 配置项：
 * - app.elasticsearch.auto-init.enabled: 是否启用自动初始化（默认true）
 * - app.elasticsearch.auto-init.sync-threshold: 同步阈值，如果ES数据量小于MySQL的此百分比，则自动同步（默认0.5，即50%）
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@Order(100) // 在CommentDataInitializer之后执行
public class PostDataInitializer implements ApplicationRunner {
    
    private final IPostRepository postRepository;
    private final PostElasticService postElasticService;
    private final PostElasticRepository postElasticRepository;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @javax.annotation.PostConstruct
    public void onInit() {
        log.info("PostDataInitializer Bean已创建，等待ApplicationRunner执行...");
    }
    
    @Value("${app.elasticsearch.auto-init.enabled:true}")
    private boolean autoInitEnabled;
    
    @Value("${app.elasticsearch.auto-init.sync-threshold:0.5}")
    private double syncThreshold;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== ES索引初始化器启动 ===");
        log.info("自动初始化配置: enabled={}, sync-threshold={}", autoInitEnabled, syncThreshold);
        
        try {
            // 获取MySQL中已发布的帖子数量
            long mysqlCount = 0;
            try {
                log.info("开始获取MySQL已发布帖子数量...");
                List<PostEntity> publishedPosts = postRepository.findAllPublished();
                mysqlCount = publishedPosts != null ? publishedPosts.size() : 0;
                log.info("MySQL已发布帖子数量: {}", mysqlCount);
            } catch (Exception e) {
                log.error("获取MySQL已发布帖子数量失败，跳过ES索引初始化检查", e);
                return;
            }

            // 获取ES索引中的帖子数量
            long esCount = 0;
            try {
                log.info("开始获取ES索引帖子数量...");
                esCount = postElasticRepository.count();
                log.info("ES索引帖子数量: {}", esCount);
            } catch (Exception e) {
                log.error("获取ES索引帖子数量失败，跳过ES索引初始化检查。错误: {}", e.getMessage(), e);
                log.error("可能的原因: 1. ES服务未启动 2. ES连接配置错误 3. ES索引不存在");
                return;
            }

            // 计算同步率
            double syncRate = mysqlCount > 0 ? (double) esCount / mysqlCount : 0;

            log.info("ES索引状态检查: MySQL已发布帖子={}, ES索引帖子={}, 同步率={}%", 
                    mysqlCount, esCount, String.format("%.2f", syncRate * 100));

            // 判断是否需要初始化
            boolean needInit = false;
            String reason = "";

            if (mysqlCount > 0 && esCount == 0) {
                // ES索引为空，需要初始化
                needInit = true;
                reason = "ES索引为空";
            } else if (mysqlCount > 0 && syncRate < syncThreshold) {
                // 同步率低于阈值，需要初始化
                needInit = true;
                reason = String.format("同步率(%.2f%%)低于阈值(%.2f%%)", syncRate * 100, syncThreshold * 100);
            }

            if (needInit) {
                if (autoInitEnabled) {
                    log.warn("检测到ES索引数据不一致({})，开始自动初始化...", reason);
                    initializeIndex();
                } else {
                    log.warn("检测到ES索引数据不一致({})，但自动初始化已禁用。", reason);
                    log.warn("请手动执行以下操作之一：");
                    log.warn("1. 调用接口: POST /api/admin/elasticsearch/reindex");
                    log.warn("2. 设置配置: app.elasticsearch.auto-init.enabled=true 并重启应用");
                    log.warn("3. 设置配置: app.elasticsearch.reindex=true 并重启应用");
                }
            } else {
                log.info("ES索引状态正常，无需初始化");
                log.info("=== ES索引初始化器检查完成 ===");
            }
        } catch (Exception e) {
            log.error("ES索引初始化检查失败", e);
            log.error("=== ES索引初始化器检查异常 ===");
        }
    }

    /**
     * 初始化ES索引
     */
    private void initializeIndex() {
        try {
            log.info("开始初始化ES帖子索引...");

            int batchSize = 100;
            int offset = 0;
            int totalIndexed = 0;
            int totalSkipped = 0;
            int totalFailed = 0;
            List<Long> failedPostIds = new java.util.ArrayList<>();

            while (true) {
                // 分批获取已发布的帖子
                List<PostEntity> posts = postRepository.findAll(offset, batchSize);

                if (posts == null || posts.isEmpty()) {
                    break;
                }

                // 索引每批帖子（只索引已发布的帖子）
                for (PostEntity post : posts) {
                    try {
                        // 只索引已发布的帖子（status = 1）
                        if (post.getStatusCode() == 1) {
                            // 使用带重试的索引方法
                            boolean success = postElasticService.indexPostWithRetry(post);
                            if (success) {
                                totalIndexed++;
                            } else {
                                log.warn("索引帖子失败: postId={}", post.getId());
                                totalFailed++;
                                failedPostIds.add(post.getId());
                                // 记录失败任务到Redis，可以后续重试
                                recordFailedIndexTask(post.getId());
                            }
                        } else {
                            totalSkipped++;
                        }
                    } catch (Exception e) {
                        log.warn("处理帖子失败: postId={}", post.getId(), e);
                        totalFailed++;
                        if (post != null && post.getId() != null) {
                            failedPostIds.add(post.getId());
                            // 记录失败任务到Redis，可以后续重试
                            recordFailedIndexTask(post.getId());
                        }
                    }
                }

                log.info("已索引 {} 个帖子，跳过 {} 个，失败 {} 个，当前批次: {} - {}", 
                        totalIndexed, totalSkipped, totalFailed, offset, offset + posts.size());

                if (posts.size() < batchSize) {
                    break;
                }

                offset += batchSize;
            }

            log.info("ES帖子索引初始化完成！共索引 {} 个帖子，跳过 {} 个，失败 {} 个", 
                    totalIndexed, totalSkipped, totalFailed);
            
            if (!failedPostIds.isEmpty()) {
                log.warn("初始化过程中有 {} 个帖子索引失败，可以通过以下方式手动重试：", failedPostIds.size());
                log.warn("1. 调用接口: POST /api/admin/elasticsearch/reindex");
                log.warn("2. 调用接口批量重试: POST /api/admin/elasticsearch/reindex/failed");
                log.warn("失败的帖子ID列表（前10个）: {}", 
                        failedPostIds.stream().limit(10).collect(java.util.stream.Collectors.toList()));
            }
            
            log.info("=== ES索引初始化完成 ===");
        } catch (Exception e) {
            log.error("ES帖子索引初始化失败", e);
            log.error("=== ES索引初始化异常 ===");
            log.error("初始化失败，可以通过以下方式手动重试：");
            log.error("1. 调用接口: POST /api/admin/elasticsearch/reindex");
            log.error("2. 检查ES服务是否正常");
            log.error("3. 检查ES连接配置是否正确");
        }
    }
    
    /**
     * 记录失败的索引任务（可以后续批量重试）
     */
    private void recordFailedIndexTask(Long postId) {
        if (redisTemplate == null || postId == null) {
            return;
        }
        try {
            String failedTasksKey = "es:index:failed:tasks";
            redisTemplate.opsForSet().add(failedTasksKey, postId.toString());
            redisTemplate.expire(failedTasksKey, 24, TimeUnit.HOURS);
            log.debug("记录失败的ES索引任务: postId={}", postId);
        } catch (Exception e) {
            log.warn("记录失败的ES索引任务失败: postId={}", postId, e);
        }
    }
}

