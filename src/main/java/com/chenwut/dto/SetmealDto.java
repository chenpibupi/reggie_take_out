package com.chenwut.dto;

import com.chenwut.entity.Setmeal;
import com.chenwut.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    //具体菜品
    private List<SetmealDish> setmealDishes;

    //套餐分类
    private String categoryName;
}
