package com.chenwut.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chenwut.entity.ShoppingCart;

/**
 *
 */

public interface ShoppingCartService extends IService<ShoppingCart> {

    ShoppingCart addOrUpdateCartItem(ShoppingCart shoppingCart);

    ShoppingCart subOrUpdateCartItem(ShoppingCart shoppingCart);

}
