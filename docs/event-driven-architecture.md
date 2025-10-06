# 事件驱动架构设计文档

## 1. 概述

本项目采用事件驱动架构（Event-Driven Architecture）来实现领域间的松耦合和业务逻辑的自动串联。通过基于Disruptor的高性能消息队列机制，各个领域可以在不直接依赖其他领域的情况下进行通信和协作。

## 2. 架构设计

### 2.1 事件发布

每个领域都定义了自己的事件发布器（EventPublisher），负责发布该领域内的业务事件：

- PostEventPublisher: 帖子领域事件发布器
- CommentEventPublisher: 评论领域事件发布器
- LikeEventPublisher: 点赞领域事件发布器
- UserEventPublisher: 用户领域事件发布器

### 2.2 事件处理

事件处理分为两个层次：

1. **Disruptor队列**: 所有事件首先发布到Disruptor高性能队列中，实现异步处理
2. **事件监听**: 通过@DisruptorListener注解标识的监听方法处理具体业务逻辑

### 2.3 事件类型

目前支持的事件类型包括：

- 用户事件: UserRegisteredEvent, UserLoggedInEvent, UserUpdatedEvent
- 帖子事件: PostCreatedEvent, PostUpdatedEvent, PostDeletedEvent
- 评论事件: CommentCreatedEvent, CommentUpdatedEvent, CommentDeletedEvent
- 点赞事件: LikeEvent

## 3. 技术实现

### 3.1 Disruptor集成

项目集成了LMAX Disruptor作为高性能消息队列：

1. **事件生产者**: EventPublisher负责将事件发布到Disruptor队列
2. **环形缓冲区**: 使用RingBuffer存储事件，支持高并发处理
3. **事件消费者**: EventConsumer从队列中消费事件并转发给Spring事件系统
4. **工作池**: 使用多个消费者线程并行处理事件

### 3.2 事件监听机制

使用自定义的@DisruptorListener注解实现事件监听：

```java
@Component
public class UserEventListener {
    
    @DisruptorListener(eventType = "PostCreatedEvent")
    public void handlePostCreated(PostCreatedEvent event) {
        // 处理帖子创建事件
    }
}
```

## 4. 使用示例

### 4.1 发布事件

在业务逻辑中发布事件：

```java
// 发布帖子创建事件
PostCreatedEvent event = PostCreatedEvent.builder()
    .postId(post.getId())
    .userId(post.getUserId())
    .title(post.getTitle().getTitle())
    .createTime(LocalDateTime.now())
    .build();
eventPublisher.publishEvent(event, "PostCreatedEvent");
```

### 4.2 监听事件

创建事件监听器处理事件：

```java
@Component
public class UserEventListener {
    
    @DisruptorListener(eventType = "PostCreatedEvent")
    public void handlePostCreated(PostCreatedEvent event) {
        // 处理帖子创建后的业务逻辑
        // 例如：增加用户积分
    }
}
```

## 5. 业务逻辑串联

通过事件机制，实现了以下业务逻辑的自动串联：

1. **用户积分系统**: 用户发帖、评论、点赞等操作会自动增加积分
2. **活跃度统计**: 用户的各种操作会自动更新活跃度
3. **通知系统**: 用户的操作会自动触发相关通知
4. **数据统计**: 各种操作会自动更新相关统计数据

## 6. 性能优势

1. **高吞吐量**: Disruptor提供比传统队列高10倍的吞吐量
2. **低延迟**: 基于环形缓冲区的设计实现纳秒级延迟
3. **无锁设计**: 减少线程竞争，提高并发性能
4. **内存效率**: 通过对象复用减少GC压力

## 7. 扩展性

要添加新的业务逻辑，只需：

1. 定义新的事件类型
2. 在相应的业务逻辑中发布事件
3. 创建事件监听器并使用@DisruptorListener注解处理事件

这种方式保证了系统的松耦合和良好的扩展性，同时通过Disruptor提供了卓越的性能表现。