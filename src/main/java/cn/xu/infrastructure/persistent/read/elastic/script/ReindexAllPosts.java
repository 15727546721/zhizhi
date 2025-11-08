package cn.xu.infrastructure.persistent.read.elastic.script;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.persistent.read.elastic.service.PostElasticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 重新索引所有帖子到Elasticsearch
 * 使用方法：在application.yml中设置 app.elasticsearch.reindex=true
 * 或者在启动时添加参数：--app.elasticsearch.reindex=true
 * 
 * 注意：此脚本会重新索引所有已发布的帖子，可能需要较长时间
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.elasticsearch.reindex", havingValue = "true", matchIfMissing = false)
public class ReindexAllPosts implements CommandLineRunner {

    private final IPostRepository postRepository;
    private final PostElasticService postElasticService;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始重新索引所有帖子到Elasticsearch...");
        
        try {
            int batchSize = 100;
            int offset = 0;
            int totalIndexed = 0;
            
            while (true) {
                // 分批获取已发布的帖子
                List<PostEntity> posts = postRepository.findAll(offset, batchSize);
                
                if (posts == null || posts.isEmpty()) {
                    break;
                }
                
                // 索引每批帖子（只索引已发布的帖子）
                for (PostEntity post : posts) {
                    try {
                        // 只索引已发布的帖子
                        if (post.getStatusCode() == 1) {
                            postElasticService.indexPost(post);
                            totalIndexed++;
                        }
                    } catch (Exception e) {
                        log.warn("索引帖子失败: postId={}", post.getId(), e);
                    }
                }
                
                log.info("已索引 {} 个帖子，当前批次: {} - {}", totalIndexed, offset, offset + posts.size());
                
                if (posts.size() < batchSize) {
                    break;
                }
                
                offset += batchSize;
            }
            
            log.info("重新索引完成！共索引 {} 个帖子", totalIndexed);
        } catch (Exception e) {
            log.error("重新索引失败", e);
            throw e;
        }
    }
}

