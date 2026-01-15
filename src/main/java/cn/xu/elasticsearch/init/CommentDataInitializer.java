package cn.xu.elasticsearch.init;

import cn.xu.elasticsearch.model.CommentIndex;
import cn.xu.elasticsearch.repository.CommentElasticRepository;
import cn.xu.model.entity.Comment;
import cn.xu.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 评论数据 ES 索引初始化
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class CommentDataInitializer implements ApplicationRunner {

    private final CommentRepository commentRepository;
    private final CommentElasticRepository commentElasticRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (commentElasticRepository.count() == 0) {
            log.info("正在初始化 Elasticsearch 评论数据...");

            int batchSize = 500;
            int processed = 0;

            while (true) {
                List<Comment> batch = commentRepository.findCommentBatch(processed, batchSize);
                if (batch.isEmpty()) {
                    break;
                }

                List<CommentIndex> indices = batch.stream()
                        .map(this::convertToIndex)
                        .collect(Collectors.toList());

                commentElasticRepository.saveAll(indices);
                processed += batch.size();

                log.info("已处理 {} 条评论数据", processed);
            }

            log.info("Elasticsearch 评论数据初始化完成，共处理 {} 条评论数据", processed);
        }
    }

    private CommentIndex convertToIndex(Comment entity) {
        CommentIndex index = new CommentIndex();
        index.setId(entity.getId());
        index.setTargetType(entity.getTargetType());
        index.setTargetId(entity.getTargetId());
        index.setParentId(entity.getParentId());
        index.setUserId(entity.getUserId());
        index.setReplyUserId(entity.getReplyUserId());
        index.setContent(entity.getContent());
        index.setLikeCount(Optional.ofNullable(entity.getLikeCount()).orElse(0L));
        index.setReplyCount(Optional.ofNullable(entity.getReplyCount()).orElse(0L));
        index.setCreateTime(entity.getCreateTime());

        long likeCount = Optional.ofNullable(entity.getLikeCount()).orElse(0L);
        long replyCount = Optional.ofNullable(entity.getReplyCount()).orElse(0L);
        long hoursSincePublish = java.time.Duration.between(entity.getCreateTime(), java.time.LocalDateTime.now()).toHours();
        double timeDecay = Math.max(0, 1 - hoursSincePublish / 72.0);
        double hotScore = likeCount + replyCount * 2 + timeDecay * 5;
        index.setHotScore(hotScore);

        return index;
    }
}
