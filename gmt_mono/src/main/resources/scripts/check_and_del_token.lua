--- KEYS[1]: 令牌在Redis中存储的key
--- ARGS[1]: 前端提交过来的令牌
---
--- 1. 获取redis中的令牌
local stored_token = redis.call("get", KEYS[1])

--- 2. 比较两个令牌
if stored_token == ARGV[1] then
    --- 3. 删除redis中的令牌
    redis.call("del", KEYS[1])
    return true  --- true 表示校验成功
else
    return false  --- false 表示校验失败
end