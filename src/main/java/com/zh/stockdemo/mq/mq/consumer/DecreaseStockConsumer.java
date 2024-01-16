/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zh.stockdemo.mq.mq.consumer;

import com.zh.stockdemo.constant.StockMQConstant;
import com.zh.stockdemo.mq.mq.domain.MessageWrapper;
import com.zh.stockdemo.mq.mq.event.DecreaseStockEvent;
import com.zh.stockdemo.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存同步消费者
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = StockMQConstant.STOCK_TOPIC_KEY,
        selectorExpression = StockMQConstant.STOCK_DEREASE_STOCK_TAG_KEY,
        consumerGroup = StockMQConstant.STOCK_DEREASE_STOCK_CG_KEY
)
public class DecreaseStockConsumer implements RocketMQListener<MessageWrapper<DecreaseStockEvent>> {

    private final StockService stockService;

    @Override
    public void onMessage(MessageWrapper<DecreaseStockEvent> message) {
        DecreaseStockEvent decreaseStockEvent = message.getMessage();
        Integer productId = decreaseStockEvent.getProductId();
        try {
            stockService.decreaseStock(productId, message.getUuid());
        } catch (Exception e) {
            log.error("库存同步到mysql失败，productId:{}", productId, e);
            throw e;
        }
    }
}
