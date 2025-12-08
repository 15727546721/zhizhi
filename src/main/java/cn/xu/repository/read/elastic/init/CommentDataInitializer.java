package cn.xu.repository.read.elastic.init;

import cn.xu.model.entity.Comment;
import cn.xu.repository.ICommentRepository;
import cn.xu.repository.read.elastic.model.CommentIndex;
import cn.xu.repository.read.elastic.repository.CommentElasticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class CommentDataInitializer implements ApplicationRunner {

    private final ICommentRepository commentRepository;
    private final CommentElasticRepository commentElasticRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (commentElasticRepository.count() == 0) {
            log.info("正在初始化 Elasticsearch 评论数据...");

            // 设置批量处理的大小
            int batchSize = 500;
            int processed = 0;

            while (true) {
                // 查询数据库中的评论数据
                List<Comment> batch = commentRepository.findCommentBatch(processed, batchSize);
                if (batch.isEmpty()) {
                    break;
                }

                // 转换为 Elasticsearch 索引对象
                List<CommentIndex> indices = batch.stream()
                        .map(this::convertToIndex)
                        .collect(Collectors.toList());

                // 保存到 Elasticsearch
                commentElasticRepository.saveAll(indices);
                processed += batch.size();

                log.info("已处理 {} 条评论数据", processed);
            }

            log.info("Elasticsearch 评论数据初始化完成，共处理 {} 条评论数据", processed);
        }
    }

    /**
     * 将 Comment 实体对象转换为 CommentIndex 索引对象
     *
     * @param entity 原始 Comment 实体对象
     * @return 转换后的 CommentIndex 索引对象
     */
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

        // 计算热度分数
        long likeCount = Optional.ofNullable(entity.getLikeCount()).orElse(0L);
        long replyCount = Optional.ofNullable(entity.getReplyCount()).orElse(0L);
        long hoursSincePublish = java.time.Duration.between(entity.getCreateTime(), java.time.LocalDateTime.now()).toHours();
        double timeDecay = Math.max(0, 1 - hoursSincePublish / 72.0);
        double hotScore = likeCount + replyCount * 2 + timeDecay * 5;
        index.setHotScore(hotScore);

        return index;
    }
}
