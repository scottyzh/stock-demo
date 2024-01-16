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

package com.zh.stockdemo.mq.mq.produce;

import cn.hutool.core.util.StrUtil;
import com.zh.stockdemo.constant.StockMQConstant;
import com.zh.stockdemo.mq.mq.domain.MessageWrapper;
import com.zh.stockdemo.mq.mq.event.DecreaseStockEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 库存同步到mysql生产者
 *
 */
@Slf4j
@Component
public class DecreaseStockProducer extends AbstractCommonSendProduceTemplate<DecreaseStockEvent> {

    private final ConfigurableEnvironment environment;

    public DecreaseStockProducer(@Autowired RocketMQTemplate rocketMQTemplate, @Autowired ConfigurableEnvironment environment) {
        super(rocketMQTemplate);
        this.environment = environment;
    }

    @Override
    protected BaseSendExtendDTO buildBaseSendExtendParam(DecreaseStockEvent messageSendEvent) {
        return BaseSendExtendDTO.builder()
                .eventName("库存同步到mysql")
                .keys(String.valueOf(messageSendEvent.getProductId()))
                .topic(environment.resolvePlaceholders(StockMQConstant.STOCK_TOPIC_KEY))
                .tag(environment.resolvePlaceholders(StockMQConstant.STOCK_DEREASE_STOCK_TAG_KEY))
                .sentTimeout(2000L)
                .build();
    }

    @Override
    protected Message<?> buildMessage(DecreaseStockEvent messageSendEvent, BaseSendExtendDTO requestParam) {
        String keys = StrUtil.isEmpty(requestParam.getKeys()) ? UUID.randomUUID().toString() : requestParam.getKeys();
        return MessageBuilder
                .withPayload(new MessageWrapper(requestParam.getKeys(), messageSendEvent))
                .setHeader(MessageConst.PROPERTY_KEYS, keys)
                .setHeader(MessageConst.PROPERTY_TAGS, requestParam.getTag())
                .build();
    }
}
