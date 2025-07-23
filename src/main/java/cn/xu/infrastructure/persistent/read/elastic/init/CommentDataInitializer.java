package cn.xu.infrastructure.persistent.read.elastic.init;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.HotScorePolicy;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.infrastructure.persistent.read.elastic.model.CommentIndex;
import cn.xu.infrastructure.persistent.read.elastic.repository.CommentElasticRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentDataInitializer implements ApplicationRunner {

    private final ICommentRepository commentRepository;
    private final CommentElasticRepository commentElasticRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (commentElasticRepository.count() == 0) {
            log.info("开始初始化ES评论数据...");

            // 分批次处理
            int batchSize = 500;
            int processed = 0;

            while (true) {
                List<CommentEntity> batch = commentRepository.findCommentBatch(processed, batchSize);
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

            log.info("ES评论数据初始化完成，共处理 {} 条数据", processed);
        }
    }

    private CommentIndex convertToIndex(CommentEntity entity) {
        CommentIndex index = new CommentIndex();
        index.setId(entity.getId());
        index.setTargetType(entity.getTargetType());
        index.setTargetId(entity.getTargetId());
        index.setParentId(entity.getParentId());
        index.setUserId(entity.getUserId());
        index.setReplyUserId(entity.getReplyUserId());
        index.setContent(entity.getContent());
        index.setLikeCount(entity.getLikeCount() != null ? entity.getLikeCount() : 0);
        index.setReplyCount(entity.getReplyCount() != null ? entity.getReplyCount() : 0);
        index.setCreateTime(entity.getCreateTime());

        // 计算热度（确保参与计算的值非 null）
        long likeCount = entity.getLikeCount() != null ? entity.getLikeCount() : 0;
        long replyCount = entity.getReplyCount() != null ? entity.getReplyCount() : 0;

        index.setHotScore(HotScorePolicy.calculate(
                likeCount,
                replyCount,
                entity.getCreateTime()
        ));

        return index;
    }
}