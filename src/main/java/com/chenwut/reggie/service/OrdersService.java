package com.chenwut.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenwut.reggie.entity.Orders;

/**
 *
 */
public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);
}
