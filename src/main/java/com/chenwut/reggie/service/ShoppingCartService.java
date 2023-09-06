package com.chenwut.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenwut.reggie.entity.ShoppingCart;

/**
 *
 */

public interface ShoppingCartService extends IService<ShoppingCart> {

    ShoppingCart addOrUpdateCartItem(ShoppingCart shoppingCart);

    ShoppingCart subOrUpdateCartItem(ShoppingCart shoppingCart);

}
