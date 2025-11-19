# 事件驱动架构设计文档

## 1. 概述

知之社区现已全面切换到 Spring Event 作为事件驱动引擎。通过 Spring 自带的 `ApplicationEventPublisher` + `@EventListener` 机制，我们实现了领域之间的解耦协作、异步扩展以及更易维护的事件总线。

## 2. 架构设计

### 2.1 事件发布

每个领域都有独立的事件发布器，负责封装领域事件并交给 Spring 事件总线：

- `PostEventPublisher`
- `CommentEventPublisher`
- `LikeEventPublisher`
- `UserEventPublisher`
- `EssayEventPublisher` 等

发布器内部统一依赖 `ApplicationEventPublisher`，并根据业务封装事件实体，例如 `PostCreatedEvent`、`CommentDeletedEvent`。

### 2.2 事件总线

基础设施层提供了 `SpringEventBus`，对外暴露了统一的 `EventBus` 接口。好处：

1. 可以集中控制事件开关（`app.event.enabled`）。
2. 统一打日志、度量指标。
3. 保持与历史 Disruptor 总线相同的调用方式，方便迁移与回滚。

### 2.3 事件监听

监听器直接使用 Spring 的 `@EventListener` 或 `@TransactionalEventListener`：

```java
@Component
public class PostEventHandler {

    @EventListener
    public void handlePostCreated(PostCreatedEvent event) {
        // 增加积分 / 刷新缓存 / 推送消息 等
    }
}
```

根据需要还可以配合 `@Async` 实现异步监听，或者通过 `@Order` 控制执行顺序。

### 2.4 事件类型

目前支持的核心事件包括：

- 用户：`UserRegisteredEvent`, `UserUpdatedEvent`
- 帖子：`PostCreatedEvent`, `PostUpdatedEvent`, `PostDeletedEvent`
- 评论：`CommentCreatedEvent`, `CommentDeletedEvent`
- 点赞/收藏：`LikeEvent`, `FavoriteEvent`
- 私信/通知等其它领域事件

## 3. 技术实现

1. **发布端**：组装事件 -> `ApplicationEventPublisher#publishEvent`。
2. **消费端**：`@EventListener` 自动注册，Spring 完成事件派发。
3. **线程模型**：默认同步；在监听器上加 `@Async("eventExecutor")` 可切到异步线程池。
4. **配置管理**：`EventConfig` 暴露 `app.event.enabled` 统一开关，可在不同环境快速启停事件。

## 4. 使用示例

### 4.1 发布事件

```java
postEventPublisher.publishCreated(postEntity);
```

发布器内部：

```java
PostCreatedEvent event = PostCreatedEvent.builder()
        .postId(post.getId())
        .userId(post.getUserId())
        .title(post.getTitle().getValue())
        .createTime(LocalDateTime.now())
        .build();
eventPublisher.publishEvent(event);
```

### 4.2 监听事件

```java
@Component
public class UserPointListener {

    @Async("eventExecutor")
    @EventListener
    public void handlePostCreated(PostCreatedEvent event) {
        userPointService.increasePoints(event.getUserId(), PointRule.POST_CREATE);
    }
}
```

## 5. 业务串联

Spring Event 驱动下，以下业务实现松耦合协作：

1. **积分与等级系统**
2. **通知与站内信**
3. **Redis / Elasticsearch 缓存同步**
4. **活跃度与热度统计**
5. **推荐、排行榜、订阅推送**

## 6. 优势

- **Spring 原生支持**：不需额外第三方依赖，维护和排查更简单。
- **统一模型**：发布/订阅接口与过去保持一致，可平滑迁移。
- **扩展性**：可灵活增加 `@EventListener`、`@Async`、`@TransactionalEventListener` 等特性。
- **可测试性**：事件是普通 POJO，配合 Spring Test 很容易写集成测试。

## 7. 扩展步骤

1. 定义事件类（POJO + Builder）。
2. 在领域服务/应用服务中调用发布器。
3. 编写监听器并加上 `@EventListener`。
4. 如需异步，在监听器上加 `@Async` 并配置线程池。

Spring Event 方案已经完全替换了旧的 Disruptor 组件（相关 Bean/依赖已移除），如需重新启用 Disruptor，可参考 `SPRING_EVENT_MIGRATION.md` 的回滚指南。*** End Patch