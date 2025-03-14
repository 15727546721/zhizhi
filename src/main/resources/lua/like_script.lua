-- 参数说明：
-- KEYS[1] 关系Key（唯一Key）
-- ARGV[1] user_id
-- ARGV[2] 操作类型（1-点赞，0-取消赞）
-- ARGV[3] 当前时间戳

local exists = redis.call('HEXISTS', KEYS[1], ARGV[1])
local action = tonumber(ARGV[2])

-- 操作类型校验
if action ~= 1 and action ~= 0 then
    return {err = "INVALID_ACTION"}
end

-- 逻辑处理
if exists == 0 then
    if action == 1 then
        -- 首次点赞
        redis.call('HSET', KEYS[1], ARGV[1], ARGV[3] .. '_1')
        return {1}  -- 点赞成功
    else
        return {3}  -- 错误码：未点赞无法取消
    end
else
    local value = redis.call('HGET', KEYS[1], ARGV[1])
    local parts = {}
    for part in string.gmatch(value, "[^_]+") do table.insert(parts, part) end
    local currentStatus = tonumber(parts[2])

    if action == 1 then
        if currentStatus == 1 then
            return {2}  -- 错误码：已点赞
        else
            -- 重新点赞（状态从 0 切回 1）
            redis.call('HSET', KEYS[1], ARGV[1], ARGV[3] .. '_1')
            return {1}  -- 点赞成功
        end
    else
        if currentStatus == 0 then
            return {4}  -- 错误码：已取消无法重复操作
        else
            -- 取消点赞（状态从 1 切到 0）
            redis.call('HSET', KEYS[1], ARGV[1], ARGV[3] .. '_0')
            return {0}  -- 取消成功
        end
    end
end