-- 秒杀脚本，用于原子性地扣减库存并记录用户
-- @param KEYS[1] 库存 key
-- @param KEYS[2] 已购买用户集合的 key
-- @param ARGV[1] 用户 id
-- @param ARGV[2] 购买数量
-- @return -1 表示用户已经购买过，0 表示库存不足，>0 表示购买成功，返回剩余库存

-- 1. 检查用户是否已经购买过
-- SISMEMBER命令：检查一个元素是否是集合的成员
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -1 -- -1 表示用户已经购买过
end

-- 2. 获取当前库存
local stock = tonumber(redis.call('get', KEYS[1]))
if (stock == nil or stock < tonumber(ARGV[2])) then
    return 0 -- 0 表示库存不足
end

-- 3. 扣减库存
local remainingStock = redis.call('decrby', KEYS[1], tonumber(ARGV[2]))

-- 4. 将用户ID添加到已购集合中
redis.call('sadd', KEYS[2], ARGV[1])

-- 5. 返回剩余库存
return remainingStock