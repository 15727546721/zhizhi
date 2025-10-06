package cn.xu.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件测试控制器
 * 用于测试事件发布功能
 */
@Slf4j
@RestController
@RequestMapping("/api/event/test")
@RequiredArgsConstructor
public class EventTestController {
    
    private final EventBus eventBus;
    
    /**
     * 测试发布简单事件
     */
    @GetMapping("/simple")
    public String testSimpleEvent() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", "这是一个测试事件");
        eventData.put("timestamp", LocalDateTime.now());
        
        eventBus.publish(eventData, "TestEvent");
        
        return "简单事件发布成功";
    }
    
    /**
     * 测试发布用户相关事件
     */
    @GetMapping("/user")
    public String testUserEvent() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("userId", 12345L);
        eventData.put("username", "testuser");
        eventData.put("action", "login");
        eventData.put("timestamp", LocalDateTime.now());
        
        eventBus.publish(eventData, "UserEvent");
        
        return "用户事件发布成功";
    }
    
    /**
     * 测试发布帖子相关事件
     */
    @GetMapping("/post")
    public String testPostEvent() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("postId", 67890L);
        eventData.put("title", "测试帖子标题");
        eventData.put("authorId", 12345L);
        eventData.put("action", "create");
        eventData.put("timestamp", LocalDateTime.now());
        
        eventBus.publish(eventData, "PostEvent");
        
        return "帖子事件发布成功";
    }
    
    /**
     * 测试同步发布事件
     */
    @GetMapping("/sync")
    public String testSyncEvent() {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("message", "这是一个同步测试事件");
        eventData.put("timestamp", LocalDateTime.now());
        
        eventBus.publishSync(eventData, "SyncTestEvent");
        
        return "同步事件发布成功";
    }
}