package com.chenwut.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chenwut.common.Result;
import com.chenwut.entity.Category;
import com.chenwut.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 保存分类，包含菜品分类和套餐分类
     *
     * @param category
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody Category category) {
        log.info("category:{}", category);
        categoryService.saveCategory(category);
        return Result.success(category.getType() == 1 ? "添加菜品分类成功！" : "添加套餐分类成功！");
    }

    /**
     * 分类信息分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize) {
        log.info("page={},pageSize={},name={}", page, pageSize);
        Page<Category> pageInfo = categoryService.getPage(page, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 删除对应ID的分类，采用逻辑删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    private Result<String> delete(Long ids) {
        log.info("将被删除的id：{}", ids);
        categoryService.deleteById(ids);
        return Result.success("分类信息删除成功");
    }

    /**
     * 修改分类信息
     *
     * @param category
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        log.info("修改分类信息为：{}", category);
        categoryService.updateById(category);
        return Result.success("修改分类信息成功");
    }

    /**
     * 获取菜品分类信息
     *
     * @param category
     * @return
     */
    /*@GetMapping("/list")
    public Result<List<Category>> list(Category category) {
        List<Category> dishCategoryList = categoryService.getDishCategoryList(category);
        return Result.success(dishCategoryList);
    }*/
    @GetMapping("/list")
    public Result<List<Category>> list(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，这里只需要判断是否为菜品（type为1是菜品，type为2是套餐）
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //查询数据
        List<Category> list = categoryService.list(queryWrapper);
        //返回数据
        return Result.success(list);
    }
}
