package com.zh.stockdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zh.stockdemo.entity.Order;
import com.zh.stockdemo.entity.StockLog;
import com.zh.stockdemo.mapper.OrderMapper;
import com.zh.stockdemo.mapper.StockLogMapper;
import com.zh.stockdemo.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zh
 * @since 2024-01-11
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;

    private final StockLogMapper stockLogMapper;

    private final RedisTemplate redisTemplate;

    private final TransactionTemplate transactionTemplate;

    private static final String LUA_DECRESE_STOCK_PATH = "lua/decreseStock.lua";

    @Override
    public void createOrder(Integer productId, Integer stockLogId) {

        // 减少Redis里面的库存
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_DECRESE_STOCK_PATH)));
        redisScript.setResultType(Long.class);


        // 执行Lua脚本
        Long redisResult = (Long) redisTemplate.execute(redisScript, Collections.singletonList(String.valueOf(productId)));

        if (redisResult < 1L) {
            throw new RuntimeException("库存售罄");
        }

        // 编程式事务
        transactionTemplate.executeWithoutResult(status -> {
            try {
                // 事务性操作
                Order order = Order.builder()
                        .productId(productId)
                        .productNum(1)
                        .build();
                orderMapper.insert(order);

                // 改stockLog
                StockLog stockLog = stockLogMapper.selectOne(new QueryWrapper<StockLog>().eq("id", stockLogId));
                if (stockLog == null) {
                    throw new RuntimeException("该库存流水不存在");
                }
                stockLog.setStatus(1);
                stockLogMapper.updateById(stockLog);
                // 如果操作成功，不抛出异常，事务将提交
            } catch (Exception e) {
                // 如果操作失败，抛出异常，事务将回滚 并且需要补偿redis的库存
                redisTemplate.opsForValue().increment(String.valueOf(productId));
                status.setRollbackOnly();
            }
        });

    }
}
