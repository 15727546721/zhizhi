package cn.xu.test;

import cn.xu.domain.post.model.valobj.PostTitle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimplifiedPostTitleTest {

    @Test
    public void testPostTitleValue() {
        // 创建PostTitle对象
        PostTitle postTitle = new PostTitle("测试标题");
        
        // 验证getValue()方法返回的是字符串
        assertNotNull(postTitle.getValue());
        assertEquals("测试标题", postTitle.getValue());
        assertTrue(postTitle.getValue() instanceof String);
        
        System.out.println("Value类型: " + postTitle.getValue().getClass().getName());
        System.out.println("Value值: " + postTitle.getValue());
        
        // 验证getTitle()方法也返回字符串
        assertNotNull(postTitle.getTitle());
        assertEquals("测试标题", postTitle.getTitle());
        assertTrue(postTitle.getTitle() instanceof String);
        
        System.out.println("Title类型: " + postTitle.getTitle().getClass().getName());
        System.out.println("Title值: " + postTitle.getTitle());
        
        // 验证toString()方法也返回字符串
        assertNotNull(postTitle.toString());
        assertEquals("测试标题", postTitle.toString());
        assertTrue(postTitle.toString() instanceof String);
        
        System.out.println("ToString类型: " + postTitle.toString().getClass().getName());
        System.out.println("ToString值: " + postTitle.toString());
    }
}