package cn.xu.repository.read.elastic.script;

import cn.xu.integration.search.strategy.ElasticsearchSearchStrategy;
import cn.xu.model.entity.Post;
import cn.xu.repository.mapper.PostMapper;
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

    private final PostMapper postMapper;
    private final ElasticsearchSearchStrategy esStrategy;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始重新索引所有帖子到Elasticsearch...");

        try {
            int batchSize = 100;
            int offset = 0;
            int totalIndexed = 0;

            while (true) {
                // 分批获取所有帖子
                List<Post> posts = postMapper.findAllWithPagination(offset, batchSize);

                if (posts == null || posts.isEmpty()) {
                    break;
                }

                // 索引每批帖子（只索引已发布的帖子）
                for (Post post : posts) {
                    try {
                        // 只索引已发布的帖子
                        if (Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
                            esStrategy.indexPost(post);
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

            log.info("重新索引完成，共索引 {} 个帖子", totalIndexed);
        } catch (Exception e) {
            log.error("重新索引失败", e);
            throw e;
        }
    }
}