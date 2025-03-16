local key = KEYS[1]
local delta = tonumber(ARGV[1])

-- 获取当前计数器值（不存在时默认为0）
local current = tonumber(redis.call('GET', key) or 0)

-- 计算新值并防止负数
local new_value = current + delta
if new_value < 0 then
    new_value = 0
end

-- 更新计数器值
redis.call('SET', key, new_value)

return new_value