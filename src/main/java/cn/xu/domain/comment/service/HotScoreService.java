package cn.xu.domain.comment.service;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.repository.ICommentRepository;
import cn.xu.infrastructure.persistent.read.elastic.model.CommentIndex;
import cn.xu.infrastructure.persistent.read.elastic.repository.CommentElasticRepository;
import cn.xu.infrastructure.persistent.read.redis.CommentRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotScoreService {

    private final ICommentRepository commentRepository;
    private final CommentRedisRepository commentRedisRepository;
    private final CommentElasticRepository commentElasticRepository;

    public void updateHotScore(Long commentId) {
        try {
            // 查询数据库中的评论
            CommentEntity comment = commentRepository.findById(commentId);
            if (comment == null) {
                log.warn("评论不存在，跳过热度更新，commentId: {}", commentId);
                return;
            }

            // 获取点赞数和回复数（从 Redis）
            long likeCount = commentRedisRepository.getLikeCount(commentId);
            long replyCount = commentRedisRepository.getReplyCount(commentId);

            // 发布时间（秒级时间戳）
            long createTime = comment.getCreateTime().toEpochSecond(ZoneOffset.UTC);

            // 时间因子（可调，12小时 = 43200秒）
            double hotScore = Math.log10(likeCount + replyCount + 1) + (createTime / 43200.0);

            // 更新到 ElasticSearch
            CommentIndex index = new CommentIndex();
            index.setId(commentId);
            index.setHotScore(hotScore);
            commentElasticRepository.save(index);

            log.info("更新评论热度成功 - commentId: {}, hotScore: {}", commentId, hotScore);

        } catch (Exception e) {
            log.error("更新评论热度失败 - commentId: {}", commentId, e);
        }
    }
}
