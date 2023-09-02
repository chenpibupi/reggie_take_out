package com.chenwut.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chenwut.entity.Category;

import java.util.List;

/**
 *
 */
public interface CategoryService extends IService<Category> {

    void saveCategory(Category category);

    Page<Category> getPage(int page, int pageSize);

    void deleteById(Long ids);

    List<Category> getDishCategoryList(Category category);
}
