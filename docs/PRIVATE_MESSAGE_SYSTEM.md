# 私信系统业务文档

## 📋 目录
- [系统概述](#系统概述)
- [核心设计思想](#核心设计思想)
- [防骚扰机制](#防骚扰机制)
- [表结构设计](#表结构设计)
- [业务流程](#业务流程)
- [关键代码](#关键代码)
- [故障排查](#故障排查)
- [API接口](#api接口)

---

## 系统概述

私信系统是一个支持用户间一对一消息通讯的功能模块，提供了完善的防骚扰机制、隐私控制和消息管理能力。

### 核心特性
- ✅ 一对一私信通讯
- ✅ 防骚扰机制（首条消息可见，后续需等待回复）
- ✅ 用户隐私设置（可设置是否接收陌生人消息）
- ✅ 用户屏蔽功能
- ✅ 消息已读/未读状态
- ✅ 对话列表管理
- ✅ 图片消息支持
- ✅ 系统级别和用户级别的权限控制

### 技术栈
- **后端**: Spring Boot + MyBatis + DDD架构
- **数据库**: MySQL 8.0
- **前端**: Vue 3 + Element Plus
- **消息状态**: 基于status字段的可见性控制

---

## 核心设计思想

### 1. 消息状态管理
通过`message`表的`status`字段控制消息可见性：

| status | 含义 | 接收方可见 | 说明 |
|--------|------|-----------|------|
| 1 | DELIVERED（已投递） | ✅ 可见 | 正常投递的消息 |
| 2 | PENDING（待投递） | ❌ 不可见 | 防骚扰拦截的消息 |
| 3 | BLOCKED（已屏蔽） | ❌ 不可见 | 被屏蔽用户的消息 |

### 2. 对话关系管理
- **conversation表**: 记录双向对话关系，首次消息时创建
- **作用**: 
  - 生成用户的对话列表
  - 记录最后消息时间
  - 优化查询性能

### 3. 首次消息记录
- **first_message表**: 记录单向首次消息，用于防骚扰
- **作用**:
  - 标记是否已发送首次消息
  - 记录对方是否已回复（has_replied字段）
  - 控制后续消息的发送权限

### 4. 用户权限控制
- **系统级别**: system_config表配置全局开关
- **用户级别**: user_message_settings表配置个人偏好
- **屏蔽关系**: user_block表记录用户间的屏蔽关系

---

## 防骚扰机制

### 设计目标
- ✅ 允许陌生人发第一条消息（让对方知道有人联系）
- ✅ 防止连续骚扰（后续消息需要等待回复）
- ✅ 建立对话后自由聊天（双向确认后正常通讯）

### 完整流程

#### 场景1: 陌生人首次发送消息

```
步骤1: A首次给B发消息
├─ 检查: first_message表无A→B记录
├─ 检查: conversation表无A-B对话
├─ 操作:
│  ├─ 保存消息 (status=1)
│  ├─ 创建first_message(A→B, has_replied=0)
│  └─ 创建conversation(A-B)
└─ 结果:
   ├─ ✅ A能看到消息（发送方视角）
   ├─ ✅ B能看到消息（status=1）
   └─ ✅ B的对话列表出现A
```

#### 场景2: 对方未回复时继续发送

```
步骤2: A继续给B发送第2条消息（B未回复）
├─ 检查: first_message(A→B).has_replied = false
├─ 操作:
│  └─ 保存消息 (status=2)
└─ 结果:
   ├─ ✅ A能看到消息（发送方视角）
   ├─ ❌ B看不到消息（防骚扰拦截）
   └─ 提示: "对方尚未回复您的消息，消息已保存但对方暂时看不到"
```

#### 场景3: 对方回复建立对话

```
步骤3: B回复A
├─ 检查: first_message(A→B)存在
├─ 操作:
│  ├─ 保存消息 (status=1)
│  ├─ 标记first_message(A→B).has_replied = true
│  └─ 更新conversation最后消息时间
└─ 结果:
   ├─ ✅ A能看到B的回复
   └─ ✅ 双方建立正常对话关系
```

#### 场景4: 建立对话后正常聊天

```
步骤4: A和B后续消息
├─ 检查: first_message(A→B).has_replied = true
├─ 操作:
│  └─ 保存消息 (status=1)
└─ 结果:
   ├─ ✅ 双方消息均可见
   └─ ✅ 正常聊天模式
```

### 特殊场景

#### 互相关注用户
```
前提: A和B互相关注
流程:
├─ 跳过防骚扰机制
├─ 所有消息 status=1
└─ 直接创建对话关系
```

#### 被屏蔽用户
```
前提: B屏蔽了A
流程:
├─ 消息保存 status=3
├─ 仅A自己可见
└─ 提示: "对方已屏蔽你，消息未送达"
```

---

## 表结构设计

### 1. message表（基础消息表）
**位置**: `message_tables.sql`

```sql
CREATE TABLE `message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID（系统消息为NULL）',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `type` TINYINT NOT NULL COMMENT '消息类型：1-系统消息 2-私信 3-点赞 4-收藏 5-评论 6-关注',
  `target_id` BIGINT DEFAULT NULL COMMENT '目标ID（文章、评论等）',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '消息状态（私信专用）：1-已投递 2-待投递 3-已屏蔽',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  KEY `idx_receiver_read` (`receiver_id`, `is_read`, `create_time`),
  KEY `idx_receiver_status` (`receiver_id`, `status`, `create_time`),
  KEY `idx_sender_receiver_status` (`sender_id`, `receiver_id`, `status`)
) COMMENT='消息表（私信、通知等）';
```

**设计要点**:
- `type=2` 表示私信消息
- `status` 字段仅私信使用，其他消息类型默认为1
- 索引优化查询性能

### 2. conversation表（对话关系表）
**位置**: `private_message_system_tables.sql`

```sql
CREATE TABLE `conversation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id_1` BIGINT NOT NULL COMMENT '用户1 ID（较小的ID）',
  `user_id_2` BIGINT NOT NULL COMMENT '用户2 ID（较大的ID）',
  `created_by` BIGINT NOT NULL COMMENT '创建者ID（首次发送消息的用户）',
  `last_message_time` DATETIME NOT NULL COMMENT '最后消息时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_user_pair` (`user_id_1`, `user_id_2`),
  KEY `idx_user1_time` (`user_id_1`, `last_message_time`),
  KEY `idx_user2_time` (`user_id_2`, `last_message_time`)
) COMMENT='私信对话关系表';
```

**设计要点**:
- `user_id_1 < user_id_2` 保证唯一性
- 首次消息时创建
- 记录最后消息时间用于排序

### 3. first_message表（首次消息记录表）
**位置**: `private_message_system_tables.sql`

```sql
CREATE TABLE `first_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `message_id` BIGINT NOT NULL COMMENT '首次消息ID',
  `has_replied` TINYINT DEFAULT 0 COMMENT '对方是否已回复：0-未回复 1-已回复',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_sender_receiver` (`sender_id`, `receiver_id`),
  KEY `idx_receiver` (`receiver_id`)
) COMMENT='首次消息记录表（防骚扰机制）';
```

**设计要点**:
- 单向记录（A→B和B→A是两条记录）
- `has_replied` 控制防骚扰逻辑
- 首次消息时创建

### 4. user_block表（用户屏蔽表）
```sql
CREATE TABLE `user_block` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `blocked_user_id` BIGINT NOT NULL COMMENT '被屏蔽的用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`),
  KEY `idx_blocked_user` (`blocked_user_id`)
) COMMENT='用户屏蔽表';
```

### 5. user_message_settings表（用户私信设置表）
```sql
CREATE TABLE `user_message_settings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `allow_stranger_message` TINYINT DEFAULT 1 COMMENT '是否允许陌生人私信：0-否 1-是',
  `allow_non_mutual_follow_message` TINYINT DEFAULT 1 COMMENT '是否允许非互相关注用户私信：0-否 1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_user_id` (`user_id`)
) COMMENT='用户私信设置表';
```

### 6. system_config表（系统配置表）
```sql
CREATE TABLE `system_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` TEXT NOT NULL COMMENT '配置值',
  `description` VARCHAR(500) COMMENT '配置描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  UNIQUE KEY `uk_config_key` (`config_key`)
) COMMENT='系统配置表';
```

**私信相关配置**:
- `private_message.enabled`: 私信功能开关
- `private_message.allow_stranger`: 是否允许陌生人私信
- `private_message.max_message_length`: 消息最大长度

---

## 业务流程

### 发送私信完整流程图

```
┌──────────────┐
│  用户A发送消息  │
└───────┬──────┘
        │
        ▼
┌─────────────────────┐
│ 1. 验证用户存在       │
│ 2. 检查系统功能开关   │
│ 3. 验证消息内容       │
│ 4. 检查发送者状态     │
└───────┬─────────────┘
        │
        ▼
   ┌────────────┐
   │ 检查屏蔽关系 │──── 是 ───▶ status=3（仅发送方可见）
   └────┬───────┘
        │ 否
        ▼
   ┌────────────┐
   │ 判断关注关系 │
   └────┬───────┘
        │
        ├─── 互相关注 ────▶ status=1（直接投递）
        │                   创建/更新conversation
        │
        └─── 非互相关注 ───▶ 检查权限设置
                           │
                           ├─ 系统不允许 → 抛出异常
                           ├─ 用户不允许 → 抛出异常
                           │
                           └─ 允许 → 进入防骚扰流程
                                    │
                                    ├─ 首次消息
                                    │  ├─ status=1
                                    │  ├─ 创建first_message
                                    │  └─ 创建conversation
                                    │
                                    ├─ 未回复前的后续消息
                                    │  └─ status=2
                                    │
                                    └─ 回复后的消息
                                       ├─ status=1
                                       └─ 标记has_replied=true
```

### 查询消息流程

```
┌────────────────┐
│ B查询对话列表    │
└────────┬───────┘
         │
         ▼
┌──────────────────────┐
│ 查询conversation表    │
│ WHERE user_id_1=B    │
│    OR user_id_2=B    │
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐
│ 返回对话列表          │
│ （包含A的对话）       │
└────────┬─────────────┘
         │
         ▼
┌────────────────────┐
│ B点击与A的对话      │
└────────┬───────────┘
         │
         ▼
┌──────────────────────────┐
│ 查询message表             │
│ WHERE type=2             │
│   AND (                  │
│     sender_id=B          │ ← B发送的（全部可见）
│     OR                   │
│     (sender_id=A AND     │
│      receiver_id=B AND   │
│      status=1)           │ ← A发送的status=1消息
│   )                      │
└────────┬─────────────────┘
         │
         ▼
┌──────────────────────┐
│ 显示消息列表          │
│ - 首条消息 ✅         │
│ - B的回复 ✅          │
│ - 后续消息 ✅         │
└──────────────────────┘
```

---

## 关键代码

### 发送消息核心逻辑

**文件**: `cn.xu.domain.message.service.PrivateMessageDomainService`

```java
@Transactional(rollbackFor = Exception.class)
public SendMessageResult sendPrivateMessage(Long senderId, Long receiverId, String content) {
    // 1. 验证用户和系统状态
    validateUsers(senderId, receiverId);
    checkSystemConfig();
    
    // 2. 检查屏蔽关系（优先级最高）
    if (userBlockRepository.existsBlock(receiverId, senderId)) {
        return saveBlockedMessage(senderId, receiverId, content);
    }
    
    // 3. 判断关注关系
    UserRelationship relationship = determineRelationship(senderId, receiverId);
    
    // 4. 发送消息
    SendMessageResult result;
    if (relationship.isMutualFollow()) {
        result = sendMutualFollowMessage(senderId, receiverId, content);
    } else {
        result = sendNonMutualFollowMessage(senderId, receiverId, content);
    }
    
    // 5. 处理回复（建立对话关系）
    if (result.getStatus().isDelivered()) {
        handleReplyMessage(senderId, receiverId);
    }
    
    return result;
}
```

### 防骚扰逻辑

```java
private SendMessageResult sendNonMutualFollowMessage(Long senderId, Long receiverId, String content) {
    Optional<FirstMessageEntity> firstMessageOpt = 
        firstMessageRepository.findBySenderAndReceiver(senderId, receiverId);
    
    if (!firstMessageOpt.isPresent()) {
        // 首次消息：status=1，创建对话
        PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
            senderId, receiverId, content, MessageStatus.DELIVERED
        );
        Long messageId = privateMessageRepository.save(aggregate);
        
        // 创建首次消息记录
        FirstMessageEntity firstMessage = FirstMessageEntity.create(senderId, receiverId, messageId);
        firstMessageRepository.save(firstMessage);
        
        // 创建对话记录（关键！）
        updateConversation(senderId, receiverId);
        
        return SendMessageResult.success(MessageStatus.DELIVERED, "消息已送达", messageId);
    } else {
        FirstMessageEntity firstMessage = firstMessageOpt.get();
        
        if (firstMessage.isNotReplied()) {
            // 未回复：status=2
            PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                senderId, receiverId, content, MessageStatus.PENDING
            );
            Long messageId = privateMessageRepository.save(aggregate);
            return SendMessageResult.success(MessageStatus.PENDING, 
                "对方尚未回复您的消息，消息已保存但对方暂时看不到", messageId);
        } else {
            // 已回复：status=1
            PrivateMessageAggregate aggregate = PrivateMessageAggregate.create(
                senderId, receiverId, content, MessageStatus.DELIVERED
            );
            Long messageId = privateMessageRepository.save(aggregate);
            updateConversation(senderId, receiverId);
            return SendMessageResult.success(MessageStatus.DELIVERED, "消息已送达", messageId);
        }
    }
}
```

### SQL查询逻辑

**文件**: `MessageMapper.xml`

```xml
<!-- 查询两个用户之间的消息 -->
<select id="selectPrivateMessagesBetweenUsers" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM message
    WHERE type = 2
    AND (
        -- 当前用户作为发送者的消息（所有状态）
        (sender_id = #{userId1} AND receiver_id = #{userId2})
        OR 
        -- 当前用户作为接收者的消息（只能看到status=1）
        (sender_id = #{userId2} AND receiver_id = #{userId1} AND status = 1)
    )
    ORDER BY create_time DESC
    LIMIT #{limit} OFFSET #{offset}
</select>
```

---

## 故障排查

### 常见问题1: 接收方看不到消息

**症状**: 发送方能看到消息，接收方看不到

**排查步骤**:
```sql
-- 1. 检查消息status
SELECT id, sender_id, receiver_id, status, content 
FROM message 
WHERE type = 2 
  AND sender_id = ? 
  AND receiver_id = ?;

-- 问题：如果所有消息status=2，首条应该是1
-- 解决：检查代码中handleFirstMessage方法
```

**解决方案**:
- 首条消息应该 status=1
- 检查 `PrivateMessageDomainService.handleFirstMessage` 方法

### 常见问题2: 对话列表看不到对方

**症状**: 消息存在，但对话列表是空的

**排查步骤**:
```sql
-- 检查conversation表
SELECT * FROM conversation 
WHERE (user_id_1 = ? AND user_id_2 = ?)
   OR (user_id_1 = ? AND user_id_2 = ?);

-- 问题：如果没有记录，说明没有创建对话
-- 解决：检查首次消息是否调用updateConversation
```

**解决方案**:
- 首次消息时必须创建conversation记录
- 检查 `updateConversation` 方法调用

### 常见问题3: 防骚扰机制失效

**症状**: 未回复前，后续消息对方也能看到

**排查步骤**:
```sql
-- 检查first_message表
SELECT * FROM first_message 
WHERE sender_id = ? AND receiver_id = ?;

-- 检查has_replied字段值
-- 问题：如果has_replied=1但对方没回复，说明逻辑错误
```

**解决方案**:
- 只有接收方回复时才标记 has_replied=true
- 检查 `handleReplyMessage` 方法

### 诊断SQL模板

```sql
-- 设置用户ID
SET @sender_id = 1;    -- 发送方A的ID
SET @receiver_id = 2;  -- 接收方B的ID

-- 查看所有相关数据
SELECT '=== 消息数据 ===' as section;
SELECT id, sender_id, receiver_id, status, LEFT(content, 30) as content 
FROM message 
WHERE type = 2 
  AND ((sender_id = @sender_id AND receiver_id = @receiver_id)
    OR (sender_id = @receiver_id AND receiver_id = @sender_id))
ORDER BY create_time DESC;

SELECT '=== 首次消息记录 ===' as section;
SELECT * FROM first_message 
WHERE (sender_id = @sender_id AND receiver_id = @receiver_id)
   OR (sender_id = @receiver_id AND receiver_id = @sender_id);

SELECT '=== 对话记录 ===' as section;
SELECT * FROM conversation 
WHERE (user_id_1 = LEAST(@sender_id, @receiver_id) 
   AND user_id_2 = GREATEST(@sender_id, @receiver_id));

-- 模拟接收方查询
SELECT '=== 接收方视角 ===' as section;
SELECT id, sender_id, receiver_id, status, LEFT(content, 30) as content 
FROM message
WHERE type = 2
AND (
    (sender_id = @receiver_id AND receiver_id = @sender_id)
    OR 
    (sender_id = @sender_id AND receiver_id = @receiver_id AND status = 1)
)
ORDER BY create_time DESC;
```

---

## API接口

### 1. 发送私信
```
POST /api/private-messages
Body: {
  "receiverId": 2,
  "content": "消息内容"
}
Response: {
  "code": 200,
  "message": "消息已送达",
  "data": {
    "messageId": 123,
    "status": 1
  }
}
```

### 2. 获取对话列表
```
GET /api/private-messages/conversations?pageNo=1&pageSize=20
Response: {
  "code": 200,
  "data": [
    {
      "userId": 2,
      "userName": "用户B",
      "userAvatar": "...",
      "lastMessage": "最后一条消息",
      "lastMessageTime": "2024-11-11 15:00:00",
      "unreadCount": 3
    }
  ]
}
```

### 3. 获取与某用户的消息列表
```
GET /api/private-messages/conversations/{userId}?pageNo=1&pageSize=20
Response: {
  "code": 200,
  "data": [
    {
      "id": 123,
      "senderId": 1,
      "receiverId": 2,
      "content": "消息内容",
      "isSelf": true,
      "isRead": true,
      "createTime": "2024-11-11 15:00:00"
    }
  ]
}
```

### 4. 标记消息已读
```
POST /api/private-messages/conversations/{userId}/read
Response: {
  "code": 200,
  "message": "标记成功"
}
```

### 5. 屏蔽用户
```
POST /api/private-messages/block/{userId}
Response: {
  "code": 200,
  "message": "屏蔽成功"
}
```

---

## 部署说明

### 数据库初始化

```bash
# 1. 创建基础消息表
mysql> SOURCE /path/to/message_tables.sql;

# 2. 创建私信系统表
mysql> SOURCE /path/to/private_message_system_tables.sql;
```

### 配置说明

在 `application.yml` 中无需特殊配置，系统配置在 `system_config` 表中：

```sql
INSERT INTO system_config (config_key, config_value, description) VALUES
('private_message.enabled', 'true', '私信功能开关'),
('private_message.allow_stranger', 'true', '是否允许陌生人私信（系统级）'),
('private_message.max_message_length', '1000', '私信最大长度（字符）');
```

---

## 维护和优化

### 性能优化建议
1. 定期清理旧消息（保留最近6个月）
2. 对话列表使用Redis缓存
3. 未读消息数使用Redis计数器
4. 消息表按时间分表（月表）

### 监控指标
- 消息发送成功率
- 防骚扰拦截率
- 对话列表查询耗时
- 消息查询耗时

---

**文档版本**: v1.0  
**最后更新**: 2024-11-11  
**维护者**: 开发团队  
