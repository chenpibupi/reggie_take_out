package com.chenwut.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chenwut.reggie.common.BaseContext;
import com.chenwut.reggie.common.CustomException;
import com.chenwut.reggie.common.Result;
import com.chenwut.reggie.entity.AddressBook;
import com.chenwut.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    public Result<AddressBook> addAddress(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("id为===={}", BaseContext.getCurrentId());

        log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return Result.success(addressBook);
    }

    @GetMapping("/list")
    public Result<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook={}", addressBook);

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> addressBooks = addressBookService.list(queryWrapper);
        return Result.success(addressBooks);
    }

    @PutMapping("/default")
    public Result<AddressBook> setDefaultAddress(@RequestBody AddressBook addressBook) {
        //获取当前用户id
        addressBook.setUserId(BaseContext.getCurrentId());
        //条件构造器
        LambdaUpdateWrapper<AddressBook> queryWrapper = new LambdaUpdateWrapper<>();
        //条件：当前用户的地址
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        //将当前用户地址的is_default字段全部设为0
        queryWrapper.set(AddressBook::getIsDefault, 0);
        //执行更新操作
        addressBookService.update(queryWrapper);
        //随后再将当前地址的is_default字段设为1
        addressBook.setIsDefault(1);
        //再次执行更新操作
        addressBookService.updateById(addressBook);
        return Result.success(addressBook);
    }

    @GetMapping("/{id}")
    public Result<AddressBook> getAddressBook(@PathVariable Long id) {
        log.info("接收到的地址id为：", id);
        if (id == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook == null) {
            throw new CustomException("地址信息不存在");
        }
        return Result.success(addressBook);
    }

    @PutMapping
    public Result<String> updateAddressBook(@RequestBody AddressBook addressBook) {
        log.info("接收到的信息为：{}", addressBook);
        if (addressBook == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        boolean updated = addressBookService.updateById(addressBook);
        if (!updated) {
            throw new CustomException("系统繁忙，请稍后再试");
        }
        return Result.success("地址修改成功");
    }

    @DeleteMapping
    public Result<String> deleteAddressBook(@RequestParam("ids") Long id) {
        if (id == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        addressBookService.removeById(id);
        return Result.success("地址删除成功");
    }

    /**
     * 结算获取地址
     * @return
     */
    @GetMapping("/default")
    public Result<AddressBook> defaultAddress() {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        //当前用户
        queryWrapper.eq(userId != null, AddressBook::getUserId, userId);
        //默认地址
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        return Result.success(addressBook);
    }
}
