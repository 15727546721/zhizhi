# 知之 — 开源社区

---

<br>

<p align="center">
    <img width="" src="src/test/4VENJ%5DW8T41X~NX%25LSHW02T.png" >
</p>

<div align="center">


[![star](https://gitee.com/xu-wq/zhizhi/badge/star.svg?theme=dark)](https://gitee.com/veal98/Echo/stargazers)
[![fork](https://gitee.com/xu-wq/zhizhi/badge/fork.svg?theme=dark)](https://gitee.com/veal98/Echo/members)

</div>



## 📚 项目简介

知之 是一套前后端分离的开源社区系统，基于目前主流 Java Web 技术栈（SpringBoot + MyBatis + MySQL + Redis + Spring Event + Lucene + Sa-Token + Vue3...），包含帖子（支持文章、讨论、问答等多种类型）、话题、评论、系统通知、点赞、关注、搜索等模块。

## 🏗️ 架构设计

### DDD架构规范

本项目严格遵循领域驱动设计（DDD）架构规范，将业务逻辑集中在领域层，基础设施关注点分离到独立的服务中。

详细规范请参考：[DDD规范文档](docs/architecture/ddd/DDD规范文档.md)

### 帖子搜索策略模式

本项目实现了帖子搜索的策略模式，支持在Elasticsearch和MySQL之间灵活切换：

1. **Elasticsearch策略**：用于全文搜索，支持分词、相关性排序等高级搜索功能
2. **MySQL策略**：作为兜底查询方案，使用LIKE查询进行简单搜索

#### 策略选择逻辑

- 可以通过配置文件中的`app.post.query.strategy`参数指定首选策略
- 如果配置的策略不可用，系统会自动按优先级选择可用策略：
  1. 优先使用Elasticsearch（如果启用且可用）
  2. 自动降级到MySQL（始终可用）
- 如果在搜索过程中Elasticsearch失败，会自动降级到MySQL进行搜索

#### 配置说明

```yaml
# Elasticsearch配置
spring:
  elasticsearch:
    enabled: true  # 启用Elasticsearch
    uris: 127.0.0.1:9200  # Elasticsearch地址

# 帖子搜索策略配置
app:
  post:
    query:
      strategy: elasticsearch  # 帖子搜索策略：elasticsearch 或 mysql
      # elasticsearch: 使用Elasticsearch进行全文搜索（推荐，需要ES服务）
      # mysql: 使用MySQL进行简单搜索（兜底方案，始终可用）
```

#### 策略特点

- **Elasticsearch策略**：
  - 支持中文分词（IK分词器）
  - 支持相关性排序
  - 支持高亮显示
  - 性能优秀，适合大数据量搜索
  - 需要Elasticsearch服务运行

- **MySQL策略**：
  - 无需额外服务
  - 实现简单，稳定可靠
  - 使用LIKE查询，性能相对较低
  - 适合小数据量或ES不可用时的兜底方案

### 测试环境配置

在测试环境中，为了确保测试的稳定性和独立性，系统会自动禁用Elasticsearch：

```yaml
# 测试环境配置 (src/test/resources/application.yml)
spring:
  elasticsearch:
    enabled: false  # 在测试环境中禁用Elasticsearch
  datasource:
    url: jdbc:mysql://127.0.0.1:13306/zhizhi?useUnicode=true&characterEncoding=utf8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&serverTimezone=UTC&useSSL=true
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

app:
  post:
    query:
      strategy: mysql  # 在测试环境中使用MySQL策略
```