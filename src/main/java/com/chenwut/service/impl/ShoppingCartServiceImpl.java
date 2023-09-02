package com.chenwut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenwut.common.BaseContext;
import com.chenwut.common.CustomException;
import com.chenwut.entity.ShoppingCart;
import com.chenwut.mapper.ShoppingCartMapper;
import com.chenwut.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 */
@Slf4j
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
        implements ShoppingCartService {

    /**
     * 加入购物车
     *
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart addOrUpdateCartItem(ShoppingCart shoppingCart) {
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        return handleAddOrUpdate(shoppingCart);
    }

    /**
     * 购物车单项删除
     *
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart subOrUpdateCartItem(ShoppingCart shoppingCart) {
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        log.info("shoppingCart: {}",shoppingCart);

        return handleSubOrUpdate(shoppingCart);
    }

    /**
     * 购物车信息删减
     * @param shoppingCart
     * @return
     */
    private ShoppingCart handleSubOrUpdate(ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        if (shoppingCart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
//            queryWrapper.eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        } else if (shoppingCart.getSetmealId() != null) {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        /**
         * 查询购物车
         */
        List<ShoppingCart> existingItems = this.list(queryWrapper);
        log.info("existingItems: {}",existingItems);

        if (!existingItems.isEmpty()) {
            //购物车存在项目
//            log.info("existingItems: {}",existingItems);
            return updateSubExistingItem(existingItems.get(0));
        } else {
            //购物车没有项目
            throw new CustomException("购物车数据拉取错误，请刷新重试");
        }
    }

    private ShoppingCart updateSubExistingItem(ShoppingCart existingItem) {
        if (existingItem.getNumber() > 1) {
            existingItem.setNumber(existingItem.getNumber() - 1);
            this.updateById(existingItem);
            return existingItem;
        } else if (existingItem.getNumber()==1){
            existingItem.setNumber(existingItem.getNumber() - 1);
            this.removeById(existingItem);
            return existingItem;
        }else {
            throw new CustomException("系统繁忙，请刷新后重试");
        }
    }


    /**
     * 新增购物车具体实现
     *
     * @param shoppingCart
     * @return
     */
    private ShoppingCart handleAddOrUpdate(ShoppingCart shoppingCart) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        if (shoppingCart.getDishId() != null) {
            queryWrapper.eq(ShoppingCart::getDishId, shoppingCart.getDishId());
        } else if (shoppingCart.getSetmealId() != null) {
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        if (shoppingCart.getDishFlavor() != null) {
//            queryWrapper.eq(ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        }

        List<ShoppingCart> existingItems = this.list(queryWrapper);

        if (!existingItems.isEmpty()) {
            return updateExistingItem(existingItems.get(0));
        } else {
            return addNewItem(shoppingCart);
        }
    }

    /**
     * 增加购物车已存在的条目数量
     *
     * @param existingItem
     * @return
     */
    private ShoppingCart updateExistingItem(ShoppingCart existingItem) {
        if (existingItem.getDishFlavor() != null) {
            existingItem.setNumber(existingItem.getNumber() + 1);
            this.updateById(existingItem);
            return existingItem;
        } else {
            existingItem.setNumber(existingItem.getNumber() + 1);
            this.updateById(existingItem);
            return existingItem;
        }
    }

    /**
     * 新增新的条目到购物车
     *
     * @param shoppingCart
     * @return
     */
    private ShoppingCart addNewItem(ShoppingCart shoppingCart) {
        shoppingCart.setCreateTime(LocalDateTime.now());
        this.save(shoppingCart);
        return shoppingCart;
    }

}




