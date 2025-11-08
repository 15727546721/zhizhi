 -- 帖子搜索性能优化索引脚本
-- 创建时间: 2025-01-08
-- 说明: 为帖子搜索功能添加索引，提升搜索性能

-- 1. 标题前缀索引（用于LIKE查询优化，支持前50个字符的前缀匹配）
-- 注意：虽然LIKE '%keyword%'无法直接使用索引，但前缀索引可以帮助优化查询计划
CREATE INDEX IF NOT EXISTS idx_post_title_prefix ON post(title(50));

-- 2. 描述字段前缀索引（用于LIKE查询优化）
CREATE INDEX IF NOT EXISTS idx_post_description_prefix ON post(description(100));

-- 3. 状态+时间复合索引（用于排序优化，status在前可以快速过滤已发布帖子）
CREATE INDEX IF NOT EXISTS idx_post_status_time ON post(status, create_time DESC);

-- 4. 状态+发布时间复合索引（用于按发布时间排序的搜索）
CREATE INDEX IF NOT EXISTS idx_post_status_publish_time ON post(status, publish_time DESC);

-- 5. MySQL 5.7+ 全文索引（推荐使用，性能最佳）
-- 注意：需要MySQL 5.7及以上版本，支持中文全文搜索
-- 如果MySQL版本低于5.7，可以注释掉以下索引
ALTER TABLE post ADD FULLTEXT INDEX IF NOT EXISTS ft_title_desc(title, description);

-- 6. 可选：如果description字段经常为空，可以只对title创建全文索引
-- ALTER TABLE post ADD FULLTEXT INDEX IF NOT EXISTS ft_title(title);

-- 索引使用说明：
-- 1. 前缀索引（idx_post_title_prefix）：
--    - 用于优化LIKE查询的执行计划
--    - 虽然不能直接加速LIKE '%keyword%'，但可以帮助MySQL优化器选择更好的执行计划
-- 
-- 2. 全文索引（ft_title_desc）：
--    - MySQL 5.7+支持中文全文搜索
--    - 可以使用MATCH...AGAINST语法替代LIKE查询
--    - 性能远优于LIKE查询
--    - 查询示例：
--      SELECT * FROM post 
--      WHERE status = 1 
--        AND MATCH(title, description) AGAINST('keyword' IN NATURAL LANGUAGE MODE)
--      ORDER BY create_time DESC;
--
-- 3. 复合索引（idx_post_status_time）：
--    - 用于优化WHERE status = 1 ORDER BY create_time DESC的查询
--    - 可以避免文件排序（filesort）

-- 查看索引创建情况
-- SHOW INDEX FROM post;

-- 查看表结构
-- DESCRIBE post;

