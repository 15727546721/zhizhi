# 日志规范文档

## 1. 日志工具类

使用 `BizLogger` 工具类统一日志格式：

```java
import cn.xu.support.log.BizLogger;
import cn.xu.support.log.LogConstants;

// 简单用法
BizLogger.of(log).module("帖子").op("创建").success("postId", postId);

// 链式调用
BizLogger.of(log)
    .module(LogConstants.MODULE_USER)
    .op(LogConstants.OP_LOGIN)
    .userId(userId)
    .param("ip", clientIp)
    .success();

// 失败日志
BizLogger.of(log)
    .module(LogConstants.MODULE_POST)
    .op(LogConstants.OP_DELETE)
    .userId(userId)
    .param("postId", postId)
    .fail("无权限删除");

// 异常日志
BizLogger.of(log)
    .module(LogConstants.MODULE_FILE)
    .op(LogConstants.OP_UPLOAD)
    .error("上传失败", e);
```

## 2. 日志格式

统一输出格式：
```
[模块] 操作结果 | 参数列表 | 原因/消息
```

示例：
```
[帖子] 创建成功 | postId=123, userId=456
[用户] 登录成功 | userId=123, ip=192.168.1.1
[帖子] 删除失败 | userId=123, postId=456 | 无权限删除
[文件] 上传异常 | fileName=test.jpg | 上传失败
```

## 3. 模块标识

使用 `LogConstants` 中定义的模块常量：

| 常量 | 值 | 说明 |
|------|-----|------|
| MODULE_USER | 用户 | 用户相关操作 |
| MODULE_POST | 帖子 | 帖子相关操作 |
| MODULE_COMMENT | 评论 | 评论相关操作 |
| MODULE_LIKE | 点赞 | 点赞相关操作 |
| MODULE_FAVORITE | 收藏 | 收藏相关操作 |
| MODULE_FOLLOW | 关注 | 关注相关操作 |
| MODULE_MESSAGE | 消息 | 消息相关操作 |
| MODULE_FILE | 文件 | 文件上传下载 |
| MODULE_AUTH | 认证 | 登录认证相关 |
| MODULE_ADMIN | 管理 | 后台管理操作 |
| MODULE_SEARCH | 搜索 | 搜索相关操作 |
| MODULE_CACHE | 缓存 | 缓存相关操作 |
| MODULE_SYSTEM | 系统 | 系统级操作 |

## 4. 操作类型

使用 `LogConstants` 中定义的操作常量：

| 常量 | 值 | 说明 |
|------|-----|------|
| OP_CREATE | 创建 | 新建资源 |
| OP_UPDATE | 更新 | 修改资源 |
| OP_DELETE | 删除 | 删除资源 |
| OP_QUERY | 查询 | 查询资源 |
| OP_LOGIN | 登录 | 用户登录 |
| OP_LOGOUT | 登出 | 用户登出 |
| OP_REGISTER | 注册 | 用户注册 |
| OP_PUBLISH | 发布 | 发布内容 |
| OP_WITHDRAW | 撤回 | 撤回内容 |
| OP_UPLOAD | 上传 | 文件上传 |

## 5. 日志级别使用规范

| 级别 | 使用场景 | 方法 |
|------|---------|------|
| DEBUG | 调试信息，生产环境不输出 | `.debug(message)` |
| INFO | 正常业务操作记录 | `.success()` / `.info(message)` |
| WARN | 业务异常、参数错误等可预期的问题 | `.fail(reason)` / `.warn(message)` |
| ERROR | 系统异常、不可预期的错误 | `.error(message, e)` |

### 级别选择原则

- **业务异常用 WARN**：如参数校验失败、权限不足、资源不存在
- **系统异常用 ERROR**：如数据库连接失败、第三方服务调用失败
- **正常操作用 INFO**：如登录成功、创建帖子成功
- **调试信息用 DEBUG**：如方法入参、中间变量

## 6. 最佳实践

### 6.1 必须记录的场景

- 用户登录/登出
- 数据创建/修改/删除
- 文件上传/下载
- 支付/订单操作
- 权限变更
- 系统配置变更

### 6.2 参数记录原则

- 记录关键业务参数（如 userId、postId）
- 不记录敏感信息（如密码、token）
- 不记录大文本内容（如帖子正文）

### 6.3 异常处理

```java
try {
    // 业务逻辑
} catch (BusinessException e) {
    // 业务异常：记录 WARN，不打印堆栈
    BizLogger.of(log).module("模块").op("操作").fail(e.getMessage());
    throw e;
} catch (Exception e) {
    // 系统异常：记录 ERROR，打印堆栈
    BizLogger.of(log).module("模块").op("操作").error("操作失败", e);
    throw new BusinessException("操作失败");
}
```

## 7. 迁移指南

将旧日志格式迁移到新格式：

```java
// 旧格式
log.info("[帖子] 创建草稿成功, userId: {}, postId: {}", userId, postId);

// 新格式
BizLogger.of(log)
    .module(LogConstants.MODULE_POST)
    .op("创建草稿")
    .userId(userId)
    .param("postId", postId)
    .success();
```

```java
// 旧格式
log.error("上传帖子封面失败", e);

// 新格式
BizLogger.of(log)
    .module(LogConstants.MODULE_FILE)
    .op(LogConstants.OP_UPLOAD)
    .error("上传帖子封面失败", e);
```
