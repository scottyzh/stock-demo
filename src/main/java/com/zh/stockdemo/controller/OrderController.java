package com.zh.stockdemo.controller;

import com.zh.stockdemo.entity.StockLog;
import com.zh.stockdemo.mq.mq.event.DecreaseStockEvent;
import com.zh.stockdemo.mq.mq.produce.DecreaseStockProducer;
import com.zh.stockdemo.service.StockLogService;
import com.zh.stockdemo.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.zh.stockdemo.service.OrderService;
import com.zh.stockdemo.entity.Order;

import java.util.Objects;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author zh
 * @since 2024-01-11
 */
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    private final StockLogService stockLogService;

    private final DecreaseStockProducer decreaseStockProducer;

    private final StockService stockService;

    private final RedisTemplate redisTemplate;

    @PostMapping(value = "/create/{id}")
    public ResponseEntity<Object> create(@PathVariable("id") Integer productId) {
        // 检查redis是否有库存0的标识
        if (redisTemplate.hasKey("product_stock_invalid_" + productId)) {
            return new ResponseEntity<>("库存不足", HttpStatus.OK);
        }

        // 先创建库存流水 这里默认一次只能扣减数量1的库存
        StockLog stockLog = StockLog.builder()
                .amount(1)
                .productId(productId)
                .status(0)
                .build();
        stockLogService.save(stockLog);

        // 发送事务消息
        try {
            DecreaseStockEvent decreaseStockEvent = DecreaseStockEvent.builder()
                    .productId(productId)
                    .stockLogId(stockLog.getId())
                    .build();
            SendResult sendResult = decreaseStockProducer.sendMessageInTransaction(decreaseStockEvent);
            if (!Objects.equals(sendResult.getSendStatus(), SendStatus.SEND_OK)) {
                log.error("事务消息发送错误，请求参数productId：{}", productId);
            }
        } catch (Exception e) {
            log.error("消息发送错误，请求参数：{}", productId, e);
        }

        return new ResponseEntity<>("created successfully", HttpStatus.OK);
    }


    @GetMapping(value = "/")
    public ResponseEntity<Page<Order>> list(@RequestParam(required = false) Integer current, @RequestParam(required = false) Integer pageSize) {
        if (current == null) {
            current = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Page<Order> aPage = orderService.page(new Page<>(current, pageSize));
        return new ResponseEntity<>(aPage, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Order> getById(@PathVariable("id") String id) {
        return new ResponseEntity<>(orderService.getById(id), HttpStatus.OK);
    }


    @PostMapping(value = "/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") String id) {
        orderService.removeById(id);
        return new ResponseEntity<>("deleted successfully", HttpStatus.OK);
    }

    @PostMapping(value = "/update")
    public ResponseEntity<Object> update(@RequestBody Order params) {
        orderService.updateById(params);
        return new ResponseEntity<>("updated successfully", HttpStatus.OK);
    }
}
