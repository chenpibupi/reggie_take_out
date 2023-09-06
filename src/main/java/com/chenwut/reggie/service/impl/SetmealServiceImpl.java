package com.chenwut.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenwut.reggie.common.CustomException;
import com.chenwut.reggie.dto.SetmealDto;
import com.chenwut.reggie.entity.Category;
import com.chenwut.reggie.entity.Setmeal;
import com.chenwut.reggie.entity.SetmealDish;
import com.chenwut.reggie.mapper.SetmealMapper;
import com.chenwut.reggie.service.CategoryService;
import com.chenwut.reggie.service.SetmealDishService;
import com.chenwut.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal>
        implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private CategoryService categoryService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal,执行insert操作
        this.save(setmealDto);

        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDto.getId()));
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public Page page(int page, int pageSize, String name) {
        //分页构造器对象
        Page<Setmeal> setmealPage = new Page<>();

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        setmealLambdaQueryWrapper.like(name != null, Setmeal::getName, name)
                .orderByDesc(Setmeal::getUpdateTime);
        this.page(setmealPage, setmealLambdaQueryWrapper);

        Page<SetmealDto> setmealDtoPage = new Page<>();

        BeanUtils.copyProperties(setmealPage, setmealDtoPage, "records");

        List<Setmeal> records = setmealPage.getRecords();

        List<SetmealDto> setmealDto = records.stream().map(new Function<Setmeal, SetmealDto>() {
            @Override
            public SetmealDto apply(Setmeal setmeal) {
                SetmealDto setmealDto = new SetmealDto();
                BeanUtils.copyProperties(setmeal, setmealDto);

                Long categoryId = setmeal.getCategoryId();
                Category category = categoryService.getById(categoryId);
                if (category != null) {
                    setmealDto.setCategoryName(category.getName());
                }
                return setmealDto;
            }
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDto);
        return setmealDtoPage;
    }

    @Override
    public void removeWithDish(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> qw = new LambdaQueryWrapper<>();
        qw.in(Setmeal::getId, ids)
                .eq(Setmeal::getStatus, 1);
        boolean exists = setmealMapper.exists(qw);
        if (exists) {
            throw new CustomException("套餐正在售卖，不能删除");
        } else {
            int count = setmealMapper.deleteBatchIds(ids);
            if (count < 0) {
                throw new CustomException("未知错误，请重试");
            }
        }
    }

    @Override
    public boolean updateDishStatusBatch(Integer status, List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId, ids);
        updateWrapper.set(Setmeal::getStatus, status);

        int updatedRows = setmealMapper.update(null, updateWrapper);
        if (updatedRows < 0) {
            throw new CustomException("未知错误，请重试");
        }
        return updatedRows > 0;
    }
}




