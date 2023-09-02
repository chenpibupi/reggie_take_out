package com.chenwut.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenwut.common.Result;
import com.chenwut.dto.DishDto;
import com.chenwut.entity.Dish;
import com.chenwut.service.CategoryService;
import com.chenwut.service.DishFlavorService;
import com.chenwut.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return Result.success("添加菜品成功");
    }

    /**
     * 分页查询菜品信息
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        log.info("page={},pageSize={},name={}", page, pageSize, name);
        Page<DishDto> pageInfo = dishService.page(page, pageSize, name);
        return Result.success(pageInfo);
    }

    /**
     * 修改菜品时页面数据回显
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> getByIdWithFlavor(@PathVariable Long id) {
        log.info("id{}", id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        log.info("查询到的数据为：{}", dishDto);
        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("接收到的数据为：{}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return Result.success("修改菜品成功");
    }

    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("接收到的参数status为{}，ids为{}", status, ids);

        boolean updated = dishService.updateDishStatusBatch(status, ids);

        if (updated) {
            return Result.success("售卖状态修改成功");
        } else {
            return Result.error("系统繁忙，请稍后再试");
        }
    }

    @DeleteMapping
    public Result<String> deleteByIds(@RequestParam List<Long> ids) {
        log.info("接收到的参数ids为{}", ids);

        dishService.deleteByIds(ids);

        return Result.success("删除菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishDto>> getlist(Dish dish) {

        List<DishDto> list = dishService.getList(dish);

        return Result.success(list);
    }
}
