package com.zh.stockdemo.service.impl;

import com.zh.stockdemo.entity.Stock;
import com.zh.stockdemo.mapper.StockMapper;
import com.zh.stockdemo.service.StockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zh
 * @since 2024-01-09
 */
@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {

    private final StockMapper stockMapper;

    private final RedisTemplate redisTemplate;

    @Override
    public int decreaseStock(Integer productId, String UUID) {
        if(redisTemplate.hasKey("decrease_mark_" + UUID)) {
            return 0;
        }
        redisTemplate.opsForValue().set("decrease_mark_" + UUID, "true", 24, TimeUnit.HOURS);
        return stockMapper.decreaseStock(productId);
    }
}
