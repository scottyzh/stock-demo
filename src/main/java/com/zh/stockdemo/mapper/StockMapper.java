package com.zh.stockdemo.mapper;

import com.zh.stockdemo.entity.Stock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zh
 * @since 2024-01-09
 */
public interface StockMapper extends BaseMapper<Stock> {

    int decreaseStock(Integer id);
}
