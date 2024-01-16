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

package com.zh.stockdemo.constant;

/**
 * RocketMQ 订单服务常量类
 *
 * @公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
public final class StockMQConstant {

    /**
     * 库存相关业务 Topic Key
     */
    public static final String STOCK_TOPIC_KEY = "stock_service_topic${unique-name:}";

    /**
     * 库存扣减业务 Tag Key
     */
    public static final String STOCK_DEREASE_STOCK_TAG_KEY = "stock_service-decrease-stock_tag${unique-name:}";


    /**
     * 减少库存同步消费者组 Key
     */
    public static final String STOCK_DEREASE_STOCK_CG_KEY = "stock_service-decrease-stock_cg${unique-name:}";


}
