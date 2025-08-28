-- 文件名: like_remove.lua
-- 描述: 取消点赞脚本，确保移除点赞并减少点赞数的操作是原子的，且不导致负数

-- KEYS[1] - 点赞集合 key（likeKey）
-- KEYS[2] - 点赞数量 key（likeCountKey）
-- ARGV[1] - 当前操作用户 ID

-- 判断用户是否已点赞
local isMember = redis.call("SISMEMBER", KEYS[1], ARGV[1])
if isMember == 0 then
    return 0 -- 未点赞，无法取消
end

-- 移除点赞
redis.call("SREM", KEYS[1], ARGV[1])

-- 点赞数减 1，防止小于 0
local current = redis.call("GET", KEYS[2])
if tonumber(current) and tonumber(current) > 0 then
    redis.call("DECR", KEYS[2])
end

return 1 -- 操作成功
