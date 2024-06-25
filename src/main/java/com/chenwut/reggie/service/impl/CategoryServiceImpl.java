package com.chenwut.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenwut.reggie.common.CustomException;
import com.chenwut.reggie.entity.Category;
import com.chenwut.reggie.entity.Dish;
import com.chenwut.reggie.entity.Setmeal;
import com.chenwut.reggie.mapper.CategoryMapper;
import com.chenwut.reggie.mapper.DishMapper;
import com.chenwut.reggie.mapper.SetmealMapper;
import com.chenwut.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void saveCategory(Category category) {
        categoryMapper.insert(category);
    }

    @Override
    public Page<Category> getPage(int page, int pageSize) {
        //1 创建IPage分页对象,设置分页参数
        Page<Category> pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<>();
        //添加条件
        qw.orderByAsc(Category::getSort);
        //2 执行分页查询
        categoryMapper.selectPage(pageInfo, qw);
        return pageInfo;
    }

    /**
     * 根据id删除分类，删除之前需要进行判断
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {

        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        boolean exists01 = dishMapper.exists(dishLambdaQueryWrapper);
        if (exists01) {
            //已经关联了菜品，抛出一个业务异常
            throw new CustomException("当前分类关联了菜品，不能删除");
        }
        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        boolean exists02 = setmealMapper.exists(setmealLambdaQueryWrapper);
        if (exists02) {
            //已经关联了套餐，抛出一个业务异常
            throw new CustomException("当前分类关联了套餐，不能删除");
        }
        //正常删除分类
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Category> getDishCategoryList(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，这里只需要判断是否为菜品（type为1是菜品，type为2是套餐）
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //查询数据
        List<Category> list = categoryMapper.selectList(queryWrapper);
        //返回数据

        return list;
    }

}




