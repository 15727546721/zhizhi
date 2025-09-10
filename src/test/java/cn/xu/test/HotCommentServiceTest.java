package cn.xu.test;

import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.service.HotCommentDomainService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 热点评论服务测试类
 * 
 * @author Lily
 */
@Slf4j
@SpringBootTest
public class HotCommentServiceTest {

    @Resource
    private HotCommentDomainService hotCommentDomainService;

    /**
     * 测试获取热门评论
     */
    @Test
    public void testGetHotComments() {
        try {
            // 假设有一个文章ID为1的热门评论
            List<CommentEntity> hotComments = hotCommentDomainService.getHotComments(
                CommentType.ARTICLE.getValue(), 1L, 1, 10);
            
            log.info("获取到{}条热门评论", hotComments.size());
            hotComments.forEach(comment -> {
                log.info("评论ID: {}, 热度分数: {}, 内容: {}", 
                        comment.getId(), comment.getHotScore(), comment.getContentValue());
            });
        } catch (Exception e) {
            log.error("测试获取热门评论失败", e);
        }
    }

    /**
     * 测试刷新热门评论缓存
     */
    @Test
    public void testRefreshHotCommentCache() {
        try {
            hotCommentDomainService.refreshHotCommentCache(CommentType.ARTICLE.getValue(), 1L);
            log.info("刷新热门评论缓存成功");
        } catch (Exception e) {
            log.error("测试刷新热门评论缓存失败", e);
        }
    }
}