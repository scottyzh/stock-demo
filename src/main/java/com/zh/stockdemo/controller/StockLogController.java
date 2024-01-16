package com.zh.stockdemo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.zh.stockdemo.service.StockLogService;
import com.zh.stockdemo.entity.StockLog;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zh
 * @since 2024-01-11
 */
@Controller
@RequestMapping("/stock-log")
public class StockLogController {


    @Autowired
    private StockLogService stockLogService;


    @GetMapping(value = "/")
    public ResponseEntity<Page<StockLog>> list(@RequestParam(required = false) Integer current, @RequestParam(required = false) Integer pageSize) {
        if (current == null) {
            current = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Page<StockLog> aPage = stockLogService.page(new Page<>(current, pageSize));
        return new ResponseEntity<>(aPage, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<StockLog> getById(@PathVariable("id") String id) {
        return new ResponseEntity<>(stockLogService.getById(id), HttpStatus.OK);
    }

    @PostMapping(value = "/create")
    public ResponseEntity<Object> create(@RequestBody StockLog params) {
        stockLogService.save(params);
        return new ResponseEntity<>("created successfully", HttpStatus.OK);
    }

    @PostMapping(value = "/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") String id) {
        stockLogService.removeById(id);
        return new ResponseEntity<>("deleted successfully", HttpStatus.OK);
    }

    @PostMapping(value = "/update")
    public ResponseEntity<Object> update(@RequestBody StockLog params) {
        stockLogService.updateById(params);
        return new ResponseEntity<>("updated successfully", HttpStatus.OK);
    }
}
