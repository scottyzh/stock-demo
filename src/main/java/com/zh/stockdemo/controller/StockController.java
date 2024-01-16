package com.zh.stockdemo.controller;

import com.zh.stockdemo.mq.mq.event.DecreaseStockEvent;
import com.zh.stockdemo.mq.mq.produce.DecreaseStockProducer;
import com.zh.stockdemo.service.StockLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.zh.stockdemo.service.StockService;

import java.util.Collections;
import java.util.Objects;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zh
 * @since 2024-01-09
 */
@Controller
@RequestMapping("/stock")
@RequiredArgsConstructor
@Slf4j
public class StockController {

    private final StockService stockService;

    private final RedisTemplate redisTemplate;

    private final StockLogService stockLogService;


    @PostMapping(value = "/decreaseStock/{id}")
    @Transactional // 开启事务
    public ResponseEntity<Object> decreaseStock(@PathVariable("id") Integer id) {

        int result = stockService.decreaseStock(id, "");
        return result == 1 ? new ResponseEntity<>("decreaseStock successfully", HttpStatus.OK) : new ResponseEntity<>("decreaseStock failed", HttpStatus.OK);
    }

    private static final String LUA_DECRESE_STOCK_PATH = "lua/decreseStock.lua";

    private final DecreaseStockProducer decreaseStockProducer;

    @PostMapping(value = "/decreaseStockByRedis/{id}")
    public ResponseEntity<Object> decreaseStockByRedis(@PathVariable("id") Integer id) {

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_DECRESE_STOCK_PATH)));
        redisScript.setResultType(Long.class);

        // 执行Lua脚本
        Long redisResult = (Long) redisTemplate.execute(redisScript, Collections.singletonList("1"));

        if (redisResult == 1) {
            // 发送消息
            try {
                DecreaseStockEvent decreaseStockEvent = DecreaseStockEvent.builder()
                        .productId(id)
                        .build();
                SendResult sendResult = decreaseStockProducer.sendMessage(decreaseStockEvent);
                if (!Objects.equals(sendResult.getSendStatus(), SendStatus.SEND_OK)) {
                    log.error("消息发送错误，请求参数：{}", id);
                }
            } catch (Exception e) {
                log.error("消息发送错误，请求参数：{}", id, e);
            }
        }

        // 返回结果判断
        return (redisResult == 1) ? new ResponseEntity<>("decreaseStock successfully", HttpStatus.OK) : new ResponseEntity<>("decreaseStock failed", HttpStatus.OK);
    }

}
