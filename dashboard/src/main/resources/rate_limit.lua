-- 参数：key, maxRequests, timeWindowSeconds
local key = KEYS[1]
local maxRequests = tonumber(ARGV[1])
local timeWindowSeconds = tonumber(ARGV[2])

local allowed = 1

local currValue = redis.call('incr', key)

if(currValue == 1) then
    redis.call('expire', key, timeWindowSeconds)
    allowed = 1
else
    if currValue > maxRequests then
        allowed = 0
    end
end

return allowed
