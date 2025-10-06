package cn.xu.test;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostContent;
import cn.xu.domain.post.model.valobj.PostStatus;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.infrastructure.persistent.converter.PostConverter;
import cn.xu.infrastructure.persistent.po.Post;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PostConverterTest {

    @Test
    public void testPostEntityToPostConversion() {
        // 创建PostEntity
        PostEntity postEntity = PostEntity.builder()
                .id(1L)
                .userId(100L)
                .categoryId(200L)
                .title(new PostTitle("测试帖子标题"))
                .content(new PostContent("测试帖子内容"))
                .description("测试描述")
                .coverUrl("http://example.com/cover.jpg")
                .type(PostType.POST)
                .status(PostStatus.PUBLISHED)
                .isFeatured(true)
                .viewCount(1000L)
                .likeCount(50L)
                .commentCount(20L)
                .collectCount(10L)
                .build();

        // 转换为PO对象
        Post post = PostConverter.toDataObject(postEntity);

        // 验证转换结果
        assertNotNull(post);
        assertEquals(1L, post.getId());
        assertEquals(100L, post.getUserId());
        assertEquals(200L, post.getCategoryId());
        assertEquals("测试帖子标题", post.getTitle()); // 这里应该是一个字符串
        assertEquals("测试帖子内容", post.getContent());
        assertEquals("测试描述", post.getDescription());
        assertEquals("http://example.com/cover.jpg", post.getCoverUrl());
        assertEquals("POST", post.getType());
        assertEquals(1, post.getStatus().intValue()); // PUBLISHED状态码为1
        assertEquals(1, post.getIsFeatured().intValue()); // true转换为1
        assertEquals(1000L, post.getViewCount());
        assertEquals(50L, post.getLikeCount());
        assertEquals(20L, post.getCommentCount());
        assertEquals(10L, post.getCollectCount());
        
        // 特别验证title字段是字符串类型
        assertNotNull(post.getTitle());
        assertEquals("测试帖子标题", post.getTitle());
        assertTrue(post.getTitle() instanceof String, "Title should be a String, but was: " + post.getTitle().getClass().getName());
        
        System.out.println("Post title type: " + post.getTitle().getClass().getName());
        System.out.println("Post title value: " + post.getTitle());
    }
}