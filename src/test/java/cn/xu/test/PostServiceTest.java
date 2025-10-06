package cn.xu.test;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostContent;
import cn.xu.domain.post.model.valobj.PostStatus;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.domain.post.model.valobj.PostType;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PostServiceTest {

    // @Resource
    // private IPostService postService;

    // @Test
    public void testCreatePost() {
        // 创建一个文章类型的帖子
        PostEntity post = PostEntity.builder()
                .title(new PostTitle("测试文章标题"))
                .content(new PostContent("这是一篇测试文章的内容，内容需要足够长才能通过验证。这是一篇测试文章的内容，内容需要足够长才能通过验证。这是一篇测试文章的内容，内容需要足够长才能通过验证。"))
                .description("测试文章描述")
                .categoryId(1L)
                .userId(1L)
                .type(PostType.POST)
                .status(PostStatus.DRAFT)
                .build();

        // 保存帖子
        // Long postId = postService.createPost(post);
        // System.out.println("创建的帖子ID: " + postId);
        System.out.println("测试方法已注释，避免实际执行");
    }
}