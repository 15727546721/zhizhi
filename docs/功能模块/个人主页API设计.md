# 个人主页后端 API 设计文档

## 📋 一、概述

本文档描述个人主页功能所需的后端 API 接口设计，包括新增接口和现有接口的扩展。

---

## 🔌 二、新增 API 接口

### 2.1 获取用户评论列表

#### 接口信息
- **路径**：`GET /api/comment/user/{userId}`
- **描述**：根据用户ID获取该用户发表的所有评论列表
- **权限**：无需登录（公开接口）

#### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数：**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNo | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| type | Integer | 否 | null | 评论类型：1-帖子评论，2-随笔评论，不传则返回所有 |

#### 请求示例
```http
GET /api/comment/user/123?pageNo=1&pageSize=10&type=1
```

#### 响应数据

**成功响应（200）：**
```json
{
  "code": 20000,
  "info": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "content": "这是一条评论内容",
        "targetId": 456,
        "targetType": 1,
        "targetTitle": "帖子标题",
        "targetUrl": "/post/456",
        "likeCount": 5,
        "replyCount": 2,
        "createTime": "2024-01-01 12:00:00",
        "updateTime": "2024-01-01 12:00:00",
        "user": {
          "id": 123,
          "nickname": "用户昵称",
          "avatar": "https://example.com/avatar.jpg"
        },
        "targetAuthor": {
          "id": 789,
          "nickname": "帖子作者",
          "avatar": "https://example.com/author.jpg"
        }
      }
    ],
    "total": 100,
    "pageNo": 1,
    "pageSize": 10,
    "totalPages": 10
  }
}
```

**错误响应：**
```json
{
  "code": 40001,
  "info": "用户不存在",
  "data": null
}
```

#### 业务逻辑

1. **查询逻辑**：
   ```sql
   SELECT c.*, 
          p.title AS target_title,
          u1.nickname, u1.avatar,
          u2.nickname AS target_author_nickname, u2.avatar AS target_author_avatar
   FROM comment c
   LEFT JOIN post p ON c.target_id = p.id AND c.target_type = 1
   LEFT JOIN user u1 ON c.user_id = u1.id
   LEFT JOIN user u2 ON p.user_id = u2.id
   WHERE c.user_id = #{userId}
     AND c.status = 1  -- 只返回正常状态的评论
     AND (c.type = #{type} OR #{type} IS NULL)
   ORDER BY c.create_time DESC
   LIMIT #{offset}, #{pageSize}
   ```

2. **统计逻辑**：
   - 评论点赞数：从 `like` 表统计
   - 评论回复数：从 `comment` 表统计（parent_id = 当前评论ID）

3. **性能优化**：
   - 使用索引：`user_id`, `target_type`, `create_time`
   - 分页查询避免全表扫描
   - 考虑使用缓存（Redis）缓存热点用户数据

---

### 2.2 获取用户点赞列表

#### 接口信息
- **路径**：`GET /api/likes/user/{userId}`
- **描述**：根据用户ID获取该用户点赞的所有内容列表
- **权限**：无需登录（公开接口）

#### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数：**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNo | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| type | String | 否 | null | 点赞类型："1"-帖子，"2"-随笔，"3"-评论，不传则返回所有 |

#### 请求示例
```http
GET /api/likes/user/123?pageNo=1&pageSize=10&type=1
```

#### 响应数据

**成功响应（200）：**
```json
{
  "code": 20000,
  "info": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "targetId": 456,
        "targetType": "1",
        "targetTitle": "帖子标题",
        "targetContent": "帖子内容预览...",
        "targetUrl": "/post/456",
        "targetCover": "https://example.com/cover.jpg",
        "likeTime": "2024-01-01 12:00:00",
        "targetAuthor": {
          "id": 789,
          "nickname": "作者昵称",
          "avatar": "https://example.com/avatar.jpg"
        },
        "targetStats": {
          "likeCount": 100,
          "commentCount": 50,
          "viewCount": 1000
        }
      }
    ],
    "total": 200,
    "pageNo": 1,
    "pageSize": 10,
    "totalPages": 20
  }
}
```

#### 业务逻辑

1. **查询逻辑**：
   ```sql
   -- 帖子类型
   SELECT l.id, l.target_id, l.type, l.create_time AS like_time,
          p.title AS target_title, p.content AS target_content,
          p.cover_url AS target_cover, p.like_count, p.comment_count, p.view_count,
          u.id AS author_id, u.nickname AS author_nickname, u.avatar AS author_avatar
   FROM `like` l
   INNER JOIN post p ON l.target_id = p.id AND l.type = '1'
   INNER JOIN user u ON p.user_id = u.id
   WHERE l.user_id = #{userId}
     AND l.status = 1
     AND (l.type = #{type} OR #{type} IS NULL)
   ORDER BY l.create_time DESC
   LIMIT #{offset}, #{pageSize}
   
   -- 随笔类型（类似）
   -- 评论类型（类似）
   ```

2. **多表关联**：
   - 根据 `targetType` 关联不同的表（post、essay、comment）
   - 使用 UNION 或分别查询后合并

3. **性能优化**：
   - 使用索引：`user_id`, `type`, `create_time`
   - 考虑分表策略（按时间分表）

---

### 2.3 获取用户话题列表

#### 接口信息
- **路径**：`GET /api/topic/user/{userId}`
- **描述**：根据用户ID获取该用户参与的话题列表
- **权限**：无需登录（公开接口）

#### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数：**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNo | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |

#### 响应数据

**成功响应（200）：**
```json
{
  "code": 20000,
  "info": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "title": "话题标题",
        "description": "话题描述",
        "cover": "https://example.com/cover.jpg",
        "participantCount": 100,
        "postCount": 50,
        "createTime": "2024-01-01 12:00:00",
        "joinTime": "2024-01-02 10:00:00"  // 用户加入时间
      }
    ],
    "total": 20,
    "pageNo": 1,
    "pageSize": 10,
    "totalPages": 2
  }
}
```

---

### 2.4 获取用户统计数据

#### 接口信息
- **路径**：`GET /api/user/{userId}/statistics`
- **描述**：获取用户的统计数据（获赞数、关注数、粉丝数等）
- **权限**：无需登录（公开接口）

#### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

#### 响应数据

**成功响应（200）：**
```json
{
  "code": 20000,
  "info": "success",
  "data": {
    "likeCount": 100,        // 获赞数（收到的点赞总数）
    "followCount": 50,       // 关注数（关注的人数）
    "fansCount": 200,        // 粉丝数（被关注的人数）
    "postCount": 30,         // 帖子数（发布的帖子数）
    "commentCount": 150,     // 评论数（发表的评论数）
    "collectionCount": 80,   // 收藏数（收藏的内容数）
    "topicCount": 10,        // 话题数（参与的话题数）
    "viewCount": 5000        // 总浏览量（所有帖子的浏览量总和）
  }
}
```

#### 业务逻辑

1. **统计逻辑**：
   ```sql
   -- 获赞数：统计用户所有内容收到的点赞
   SELECT COUNT(*) FROM `like` l
   INNER JOIN post p ON l.target_id = p.id AND l.type = '1'
   WHERE p.user_id = #{userId} AND l.status = 1
   -- 加上随笔、评论的点赞数
   
   -- 关注数：从 follow 表统计
   SELECT COUNT(*) FROM follow WHERE follower_id = #{userId} AND status = 1
   
   -- 粉丝数：从 follow 表统计
   SELECT COUNT(*) FROM follow WHERE followed_id = #{userId} AND status = 1
   
   -- 帖子数：从 post 表统计
   SELECT COUNT(*) FROM post WHERE user_id = #{userId} AND status = 'PUBLISHED'
   
   -- 评论数：从 comment 表统计
   SELECT COUNT(*) FROM comment WHERE user_id = #{userId} AND status = 1
   
   -- 收藏数：从 favorite 表统计
   SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND status = 1
   ```

2. **性能优化**：
   - 使用 Redis 缓存统计数据
   - 缓存过期时间：5分钟
   - 数据更新时清除缓存
   - 考虑使用定时任务异步更新统计数据

---

## 🔄 三、现有接口扩展

### 3.1 获取用户帖子列表（扩展）

#### 接口信息
- **路径**：`GET /api/post/user/{userId}`
- **描述**：根据用户ID获取该用户发布的帖子列表（扩展现有接口）
- **权限**：无需登录（公开接口）

#### 请求参数

**路径参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**查询参数：**
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| pageNo | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页数量 |
| status | String | 否 | PUBLISHED | 帖子状态：PUBLISHED-已发布，DRAFT-草稿 |
| type | String | 否 | null | 帖子类型，不传则返回所有 |

#### 响应数据

**成功响应（200）：**
```json
{
  "code": 20000,
  "info": "success",
  "data": {
    "list": [
      {
        "id": 1,
        "title": "帖子标题",
        "description": "帖子描述",
        "content": "帖子内容",
        "cover": "https://example.com/cover.jpg",
        "type": "ARTICLE",
        "status": "PUBLISHED",
        "likeCount": 100,
        "commentCount": 50,
        "viewCount": 1000,
        "createTime": "2024-01-01 12:00:00",
        "tags": ["标签1", "标签2"],
        "user": {
          "id": 123,
          "nickname": "用户昵称",
          "avatar": "https://example.com/avatar.jpg"
        }
      }
    ],
    "total": 50,
    "pageNo": 1,
    "pageSize": 10,
    "totalPages": 5
  }
}
```

---

## 📊 四、数据模型设计

### 4.1 响应对象设计

#### CommentListItemResponse
```java
@Data
@Builder
public class CommentListItemResponse {
    private Long id;
    private String content;
    private Long targetId;
    private Integer targetType;
    private String targetTitle;
    private String targetUrl;
    private Integer likeCount;
    private Integer replyCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private UserInfoResponse user;
    private UserInfoResponse targetAuthor;
}
```

#### LikeListItemResponse
```java
@Data
@Builder
public class LikeListItemResponse {
    private Long id;
    private Long targetId;
    private String targetType;
    private String targetTitle;
    private String targetContent;
    private String targetUrl;
    private String targetCover;
    private LocalDateTime likeTime;
    private UserInfoResponse targetAuthor;
    private ContentStatsResponse targetStats;
}
```

#### UserStatisticsResponse
```java
@Data
@Builder
public class UserStatisticsResponse {
    private Long likeCount;        // 获赞数
    private Long followCount;      // 关注数
    private Long fansCount;        // 粉丝数
    private Long postCount;        // 帖子数
    private Long commentCount;     // 评论数
    private Long collectionCount;  // 收藏数
    private Long topicCount;       // 话题数
    private Long viewCount;        // 总浏览量
}
```

---

## ⚡ 五、性能优化方案

### 5.1 数据库优化

1. **索引设计**：
   ```sql
   -- comment 表
   CREATE INDEX idx_user_id_create_time ON comment(user_id, create_time DESC);
   CREATE INDEX idx_target_type ON comment(target_type, target_id);
   
   -- like 表
   CREATE INDEX idx_user_id_type_create_time ON `like`(user_id, type, create_time DESC);
   CREATE INDEX idx_target_type ON `like`(target_id, type);
   
   -- follow 表
   CREATE INDEX idx_follower_id ON follow(follower_id, status);
   CREATE INDEX idx_followed_id ON follow(followed_id, status);
   ```

2. **分页优化**：
   - 使用 LIMIT + OFFSET，但避免深度分页
   - 考虑使用游标分页（cursor-based pagination）

### 5.2 缓存策略

1. **统计数据缓存**：
   ```java
   // Redis Key 设计
   String key = "user:statistics:" + userId;
   // 缓存时间：5分钟
   // 更新策略：数据变更时清除缓存
   ```

2. **列表数据缓存**：
   ```java
   // Redis Key 设计
   String key = "user:comments:" + userId + ":page:" + pageNo;
   // 缓存时间：2分钟
   // 更新策略：用户发表新评论时清除相关缓存
   ```

### 5.3 异步处理

1. **统计数据异步更新**：
   - 使用消息队列（如 RabbitMQ）异步更新统计数据
   - 避免实时统计影响接口性能

2. **定时任务**：
   - 定时刷新热点用户的统计数据
   - 清理过期缓存

---

## 🔒 六、安全考虑

### 6.1 权限控制

1. **公开接口**：
   - 获取用户评论列表
   - 获取用户点赞列表
   - 获取用户统计数据
   - 这些接口无需登录即可访问

2. **私有接口**：
   - 获取自己的草稿列表（需要登录）
   - 删除自己的内容（需要登录且验证所有权）

### 6.2 数据安全

1. **敏感信息过滤**：
   - 不返回用户的手机号、邮箱等敏感信息（除非是自己的）
   - 评论内容需要过滤敏感词

2. **SQL 注入防护**：
   - 使用参数化查询
   - 使用 MyBatis 的 #{} 而不是 ${}

3. **XSS 防护**：
   - 对用户输入进行转义
   - 使用安全的 JSON 序列化

---

## 📝 七、接口实现示例

### 7.1 Controller 示例

```java
@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论接口")
@Slf4j
public class CommentController {
    
    @Resource
    private ICommentService commentService;
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户评论列表")
    public ResponseEntity<PageResponse<List<CommentListItemResponse>>> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer type) {
        
        try {
            PageResponse<List<CommentListItemResponse>> response = 
                commentService.getUserComments(userId, pageNo, pageSize, type);
            return ResponseEntity.success(response);
        } catch (Exception e) {
            log.error("获取用户评论列表失败, userId: {}", userId, e);
            return ResponseEntity.error("获取评论列表失败");
        }
    }
}
```

### 7.2 Service 示例

```java
@Service
@Slf4j
public class CommentServiceImpl implements ICommentService {
    
    @Resource
    private CommentRepository commentRepository;
    
    @Override
    public PageResponse<List<CommentListItemResponse>> getUserComments(
            Long userId, Integer pageNo, Integer pageSize, Integer type) {
        
        // 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        // 分页参数校验
        pageNo = Math.max(1, pageNo);
        pageSize = Math.min(100, Math.max(1, pageSize));
        
        // 查询评论列表
        List<CommentEntity> comments = commentRepository.findByUserId(
            userId, type, pageNo, pageSize);
        
        // 查询总数
        Long total = commentRepository.countByUserId(userId, type);
        
        // 转换为响应对象
        List<CommentListItemResponse> responseList = comments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        // 构建分页响应
        return PageResponse.ofList(pageNo, pageSize, total, responseList);
    }
    
    private CommentListItemResponse convertToResponse(CommentEntity entity) {
        // 转换逻辑
        return CommentListItemResponse.builder()
            .id(entity.getId())
            .content(entity.getContent())
            // ... 其他字段
            .build();
    }
}
```

---

## ✅ 八、测试用例

### 8.1 单元测试

```java
@SpringBootTest
class CommentServiceTest {
    
    @Test
    void testGetUserComments() {
        // 测试正常情况
        // 测试分页
        // 测试类型筛选
        // 测试空数据
        // 测试异常情况
    }
}
```

### 8.2 接口测试

使用 Postman 或 Swagger 测试：
- 正常请求
- 参数校验
- 边界值测试
- 性能测试

---

**文档版本：** v1.0  
**最后更新：** 2024年  
**维护者：** 后端开发团队

