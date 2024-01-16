package com.zh.stockdemo.service;

import com.zh.stockdemo.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zh
 * @since 2024-01-11
 */
public interface OrderService extends IService<Order> {

    void createOrder(Integer productId , Integer stockLogId);
}
