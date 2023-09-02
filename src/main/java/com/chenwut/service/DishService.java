package com.chenwut.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenwut.dto.DishDto;
import com.chenwut.entity.Dish;

import java.util.List;

/**
 *
 */
public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    Page page(int page, int pageSize, String name);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);


    boolean updateDishStatusBatch(Integer status, List<Long> ids);

    void deleteByIds(List<Long> ids);

    List<DishDto> getList(Dish dish);
}
