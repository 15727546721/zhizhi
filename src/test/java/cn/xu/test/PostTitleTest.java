package cn.xu.test;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostContent;
import cn.xu.domain.post.model.valobj.PostStatus;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.infrastructure.persistent.converter.PostConverter;
import cn.xu.infrastructure.persistent.po.Post;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PostTitleTest {

    @Test
    public void testPostTitleConversion() {
        // 创建PostEntity
        PostEntity postEntity = PostEntity.builder()
                .userId(1L)
                .categoryId(1L)
                .title(new PostTitle("测试标题"))
                .content(new PostContent("测试内容"))
                .type(PostType.POST)
                .status(PostStatus.DRAFT)
                .build();

        // 转换为PO对象
        Post post = PostConverter.toDataObject(postEntity);

        // 验证title字段是字符串而不是PostTitle对象
        assertNotNull(post.getTitle());
        assertEquals("测试标题", post.getTitle());
        assertTrue(post.getTitle() instanceof String);
        
        System.out.println("Title类型: " + post.getTitle().getClass().getName());
        System.out.println("Title值: " + post.getTitle());
    }
}