# PostService 迁移指南

## 概述

`PostService` 作为门面类已被标记为 `@Deprecated`。为了更好的代码组织和维护性，建议直接使用具体的服务类：

- **PostQueryService** - 所有查询操作
- **PostCommandService** - 所有增删改操作  
- **PostStatisticsService** - 所有统计操作

## 迁移步骤

### 1. 更新依赖注入

**旧代码：**
```java
@Autowired
private PostService postService;
```

**新代码：**
```java
@Autowired
private PostQueryService postQueryService;

@Autowired
private PostCommandService postCommandService;

@Autowired
private PostStatisticsService postStatisticsService;
```

### 2. 更新方法调用

#### 查询操作

| 旧方法 (PostService) | 新方法 (PostQueryService) |
|---------------------|--------------------------|
| `getPostById(id)` | `getById(id)` |
| `getPostWithTags(id)` | `getWithTags(id)` |
| `getUserPosts(userId)` | `getByUserId(userId)` |
| `getLatestPosts(page, size)` | `getAll(page, size)` |
| `getHotPosts(page, size)` | `getHotPosts(page, size)` |
| `searchPosts(keyword, offset, limit)` | `search(keyword, offset, limit)` |

**示例：**
```java
// 旧代码
Optional<Post> post = postService.getPostById(postId);

// 新代码
Optional<Post> post = postQueryService.getById(postId);
```

#### 命令操作

| 旧方法 (PostService) | 新方法 (PostCommandService) |
|---------------------|----------------------------|
| `createDraft(...)` | `createDraft(...)` |
| `publishPost(...)` | `publishPost(...)` |
| `deletePost(...)` | `deletePost(...)` |
| `increaseLikeCount(id)` | `increaseLikeCount(id)` |
| `viewPost(id, userId, ip)` | `viewPost(id, userId, ip)` |

**示例：**
```java
// 旧代码
postService.publishPost(postId, userId, title, content, desc, cover, tags);

// 新代码
postCommandService.publishPost(postId, userId, title, content, desc, cover, tags);
```

#### 统计操作

| 旧方法 (PostService) | 新方法 (PostStatisticsService) |
|---------------------|-------------------------------|
| `countAllPosts()` | `countAll()` |
| `countUserPublishedPosts(userId)` | `countPublishedByUserId(userId)` |
| `countPostsByTag(tagId)` | `countByTagId(tagId)` |
| `countSearchPosts(keyword)` | `countSearch(keyword)` |

**示例：**
```java
// 旧代码
long count = postService.countAllPosts();

// 新代码
long count = postStatisticsService.countAll();
```

## 优势

1. **职责清晰** - 每个服务类只负责一类操作
2. **易于维护** - 代码组织更清晰，修改影响范围更小
3. **性能优化** - 可以针对不同类型的操作进行专门优化
4. **测试友好** - 更容易编写单元测试和 Mock

## 时间表

- **当前版本** - PostService 标记为 @Deprecated，但仍可使用
- **下一版本** - 建议完成迁移
- **未来版本** - 可能移除 PostService

## 注意事项

1. 迁移过程中保持向后兼容
2. 优先迁移新功能和活跃模块
3. 遗留代码可以逐步迁移
4. 确保充分测试迁移后的代码

## 需要帮助？

如有疑问，请查看具体服务类的 JavaDoc 或联系开发团队。
