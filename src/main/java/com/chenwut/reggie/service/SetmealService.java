package com.chenwut.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenwut.reggie.dto.SetmealDto;
import com.chenwut.reggie.entity.Setmeal;

import java.util.List;

/**
 *
 */
public interface SetmealService extends IService<Setmeal> {

    void saveWithDish(SetmealDto setmealDto);

    Page page(int page, int pageSize, String name);

    void removeWithDish(List<Long> ids);

    boolean updateDishStatusBatch(Integer status, List<Long> ids);
}
