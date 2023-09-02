package com.chenwut.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenwut.entity.Orders;

/**
 *
 */
public interface OrdersService extends IService<Orders> {

    void submit(Orders orders);
}
