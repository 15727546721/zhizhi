-- 文件名: like_add.lua
-- 描述: 点赞操作脚本，确保添加点赞并自增点赞数的操作是原子的

-- KEYS[1] - 点赞集合 key（likeKey）
-- KEYS[2] - 点赞数量 key（likeCountKey）
-- ARGV[1] - 当前操作用户 ID

-- 如果用户已经在集合中，表示已点赞
local isMember = redis.call("SISMEMBER", KEYS[1], ARGV[1])
if isMember == 1 then
    return 0 -- 已点赞，操作失败
end

-- 添加到点赞集合
redis.call("SADD", KEYS[1], ARGV[1])

-- 点赞数 +1
redis.call("INCR", KEYS[2])

return 1 -- 操作成功
