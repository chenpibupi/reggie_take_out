package com.chenwut.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chenwut.common.BaseContext;
import com.chenwut.common.Result;
import com.chenwut.entity.OrderDetail;
import com.chenwut.entity.ShoppingCart;
import com.chenwut.service.OrderDetailService;
import com.chenwut.service.OrdersService;
import com.chenwut.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = BaseContext.getCurrentId();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        return Result.success(shoppingCarts);
    }

    @PostMapping("/add")
    public Result<ShoppingCart> addCartItem(@RequestBody ShoppingCart shoppingCart) {
        ShoppingCart updatedCartItem = shoppingCartService.addOrUpdateCartItem(shoppingCart);
        return Result.success(updatedCartItem);
    }
    @PostMapping("/sub")
    public Result<ShoppingCart> subCartItem(@RequestBody ShoppingCart shoppingCart){
        log.info("接收到的数据为：{}",shoppingCart);
        ShoppingCart updatedCartItem = shoppingCartService.subOrUpdateCartItem(shoppingCart);
        return Result.success(updatedCartItem);
    }

    @DeleteMapping("/clean")
    public Result<String> clean() {
        //条件构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        queryWrapper.eq(userId != null, ShoppingCart::getUserId, userId);
        //删除当前用户id的所有购物车数据
        shoppingCartService.remove(queryWrapper);
        return Result.success("成功清空购物车");
    }

    @PostMapping("/again")
    public Result<String> again(@RequestBody Map<String,String> map){
        //获取order_id
        Long orderId = Long.valueOf(map.get("id"));
        //条件构造器
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        //查询订单的口味细节数据
        queryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> details = orderDetailService.list(queryWrapper);
        //获取用户id，待会需要set操作
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCarts = details.stream().map((item) ->{
            ShoppingCart shoppingCart = new ShoppingCart();
            //Copy对应属性值
            BeanUtils.copyProperties(item,shoppingCart);
            //设置一下userId
            shoppingCart.setUserId(userId);
            //设置一下创建时间为当前时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //加入购物车
        shoppingCartService.saveBatch(shoppingCarts);
        return Result.success("喜欢吃就再来一单吖~");
    }
}
