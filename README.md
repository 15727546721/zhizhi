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

知之 是一套前后端分离的开源社区系统，基于目前主流 Java Web 技术栈（SpringBoot + MyBatis + MySQL + Redis + Disruptor + Lucene + Sa-Token + Vue3...），包含文章、话题、评论、系统通知、点赞、关注、搜索等模块。

## 🏗️ 架构设计

### 文章查询策略模式

本项目实现了文章查询的策略模式，支持在Elasticsearch和MySQL之间切换：

1. **Elasticsearch策略**：用于全文搜索和热门文章排行
2. **MySQL策略**：作为兜底查询方案

可以通过配置文件中的`app.article.query.strategy`参数来切换策略：
- `elasticsearch`：使用Elasticsearch查询（默认）
- `mysql`：使用MySQL查询

当Elasticsearch不可用时，系统会自动回退到MySQL查询。

### 配置说明

```yaml
# Elasticsearch配置
spring:
  elasticsearch:
    enabled: true  # 启用Elasticsearch
    uris: 127.0.0.1:9200  # Elasticsearch地址

# 文章查询策略配置
app:
  article:
    query:
      strategy: elasticsearch  # 文章查询策略：elasticsearch 或 mysql
```

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
  article:
    query:
      strategy: mysql  # 在测试环境中使用MySQL策略
```