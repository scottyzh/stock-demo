local key = KEYS[1]

-- 检查键是否存在
local exists = redis.call('EXISTS', key)
if exists == 1 then
    -- 键存在，获取值
    local value = redis.call('GET', key)
    if tonumber(value) > 0 then
        -- 如果值大于0，则递减
        redis.call('DECR', key)
        return 1  -- 表示递减成功
    else
        local prefix = "product_stock_invalid_"
        local stock_invalid_tag = prefix .. KEYS[1]
        local exists_tag = redis.call('EXISTS', stock_invalid_tag)
        if exists_tag == 0 then
            -- 键不存在，设置键的值
            redis.call('SET', stock_invalid_tag, "true")
        return 0  -- 表示递减失败，值不大于0
        end
    end
else
    return -1  -- 表示递减失败，键不存在
end