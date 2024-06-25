package com.chenwut.reggie.dto;

import com.chenwut.reggie.entity.Setmeal;
import com.chenwut.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    //具体菜品
    private List<SetmealDish> setmealDishes;

    //套餐分类
    private String categoryName;
}
