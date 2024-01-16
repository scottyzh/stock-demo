package com.zh.stockdemo.service;

import com.zh.stockdemo.entity.Stock;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zh
 * @since 2024-01-09
 */
public interface StockService extends IService<Stock> {

    int decreaseStock(Integer productId, String UUID);
}
