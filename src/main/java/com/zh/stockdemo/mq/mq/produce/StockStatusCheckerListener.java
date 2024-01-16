package com.zh.stockdemo.mq.mq.produce;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zh.stockdemo.entity.StockLog;
import com.zh.stockdemo.mq.mq.domain.MessageWrapper;
import com.zh.stockdemo.mq.mq.event.DecreaseStockEvent;
import com.zh.stockdemo.service.OrderService;
import com.zh.stockdemo.service.StockLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@RocketMQTransactionListener
@RequiredArgsConstructor
public class StockStatusCheckerListener implements RocketMQLocalTransactionListener {

    private final OrderService orderService;

    private final StockLogService stockLogService;

    private final TransactionTemplate transactionTemplate;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        log.info("message: {}, args: {}", message, arg);
        TypeReference<MessageWrapper<DecreaseStockEvent>> typeReference = new TypeReference<MessageWrapper<DecreaseStockEvent>>() {};
        MessageWrapper<DecreaseStockEvent> messageWrapper = JSON.parseObject(new String((byte[]) message.getPayload()), typeReference);
        DecreaseStockEvent decreaseStockEvent = messageWrapper.getMessage();
        log.info("decreaseStockEvent info : {}", decreaseStockEvent);
        try {
            orderService.createOrder(decreaseStockEvent.getProductId(), decreaseStockEvent.getStockLogId());
        } catch (Exception e) {
            log.error("插入订单失败, decreaseStockEvent info : {}", decreaseStockEvent, e);
            // 触发回查
            //设置对应的stockLog为回滚状态
            StockLog stockLog = stockLogService.getOne(new QueryWrapper<StockLog>().eq("id", decreaseStockEvent.getStockLogId()));
            stockLog.setStatus(2);
            stockLogService.updateById(stockLog);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        return RocketMQLocalTransactionState.COMMIT;
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        log.info("message: {}, args: {}", message);
        MessageWrapper<DecreaseStockEvent> messageWrapper = (MessageWrapper) message.getPayload();
        DecreaseStockEvent decreaseStockEvent = messageWrapper.getMessage();
        StockLog stockLog = stockLogService.getOne(new QueryWrapper<StockLog>().eq("id", decreaseStockEvent.getStockLogId()));
        if (stockLog == null) {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
        // 已经被扣减了库存
        if (stockLog.getStatus().intValue() == 1) {
            return RocketMQLocalTransactionState.COMMIT;
            // 初始化状态
        } else if (stockLog.getStatus().intValue() == 0) {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }

}
