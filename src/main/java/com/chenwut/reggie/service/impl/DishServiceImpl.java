package com.chenwut.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chenwut.reggie.common.CustomException;
import com.chenwut.reggie.dto.DishDto;
import com.chenwut.reggie.entity.Category;
import com.chenwut.reggie.entity.Dish;
import com.chenwut.reggie.entity.DishFlavor;
import com.chenwut.reggie.mapper.CategoryMapper;
import com.chenwut.reggie.mapper.DishMapper;
import com.chenwut.reggie.service.CategoryService;
import com.chenwut.reggie.service.DishFlavorService;
import com.chenwut.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
        implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 菜品添加
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品基本信息到菜品表dish
        this.save(dishDto);
        //获取菜品id
        Long dishId = dishDto.getId();

        //清理Redis中的缓存
        //1.清理所有分类的缓存
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        //2.清理某个分类下的缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);


        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();

        //菜品口味表flavors缺少菜品id，需要设置菜品id ---------- 采用Lambda表达式进行菜品id设置
        //1.使用Lambda表达式设置
        flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
        log.info("使用Lambda表达式处理" + flavors);

        /**
         //2.使用stream流中的map、collect方法，进行设置后再收集到一个新的集合中
         flavors = flavors.stream()
         .map(dishFlavor -> {
         dishFlavor.setDishId(dishId);
         return dishFlavor;
         }).collect(Collectors.toList());
         log.info("使用Stream流处理" + flavors);
         */
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public Page page(int page, int pageSize, String name) {
        //1 创建IPage分页对象,设置分页参数
        Page<Dish> dishPage = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        //添加条件
        qw.eq(StringUtils.isNotBlank(name), Dish::getName, name)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);
        //2 执行分页查询
        dishMapper.selectPage(dishPage, qw);

        //查询结果缺少菜品分类名称，需要使用以DishDto做分页查询
        Page<DishDto> dishDtoPage = new Page<>();

        //将dishPage拷贝给dishDtoPage
        //对象拷贝
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");

        //将records的数据转为dtoRecords
        List<Dish> records = dishPage.getRecords();
        List<DishDto> dtoRecords = records.stream()
                .map(item -> {
                    DishDto dishDto = new DishDto();
                    //将Dish对象拷贝给DishDto对象
                    BeanUtils.copyProperties(item, dishDto);
                    //获取categoryId对应的categoryName
                    Long categoryId = item.getCategoryId();
                    Category category = categoryService.getById(categoryId);
                    dishDto.setCategoryName(category.getName());
                    return dishDto;
                }).collect(Collectors.toList());
        //设置dishDtoPage的records
        dishDtoPage.setRecords(dtoRecords);
        return dishDtoPage;
    }

    /**
     * 修改菜品时数据回显
     *
     * @param id
     * @return
     */

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //先根据id查询到对应的dish对象
        Dish dish = this.getById(id);
        //创建一个dishDao对象
        DishDto dishDto = new DishDto();
        //拷贝对象
        BeanUtils.copyProperties(dish, dishDto);
        //条件构造器，对DishFlavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //根据dish_id来查询对应的菜品口味数据
        queryWrapper.eq(DishFlavor::getDishId, id);
        //获取查询的结果
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        //并将其赋给dishDto
        dishDto.setFlavors(flavors);
        //作为结果返回给前端
        return dishDto;
    }


    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新当前菜品数据（dish表）
        this.updateById(dishDto);

        //清理Redis中的缓存
        //1.清理所有分类的缓存
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        //2.清理某个分类下的缓存
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);



        //下面是更新当前菜品的口味数据----------先删除再添加
        //条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //条件是当前菜品id
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        //将其删除掉
        dishFlavorService.remove(queryWrapper);
        //获取传入的新的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        //这些口味数据还是没有dish_id，所以需要赋予其dishId
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        //再重新加入到表中
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public boolean updateDishStatusBatch(Integer status, List<Long> ids) {
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Dish::getId, ids);
        updateWrapper.set(Dish::getStatus, status);

        int updatedRows = dishMapper.update(null, updateWrapper);
        return updatedRows > 0;
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        LambdaQueryWrapper<Dish> qw = new LambdaQueryWrapper<>();
        qw.in(Dish::getId, ids)
                .eq(Dish::getStatus, 1);
        boolean exists = dishMapper.exists(qw);

        if (exists) {
            throw new CustomException("菜品正在售卖，不能删除");
        } else {
            int count = dishMapper.deleteBatchIds(ids);
            if (count < 0) {
                throw new CustomException("未知错误，请重试");
            }
        }
    }


    /*@Override
    public List<Dish> getList(Dish dish) {
        //条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //根据传进来的categoryId查询
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //只查询状态为1的菜品（启售菜品）
        queryWrapper.eq(Dish::getStatus, 1);
        //简单排下序，其实也没啥太大作用
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //获取查询到的结果作为返回值
        List<Dish> list = dishMapper.selectList(queryWrapper);
        return list;
    }*/
    @Override
    public List<DishDto> getList(Dish dish) {

        List<DishDto> dtoList = null;

        //设置Redis中的key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        dtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //1.从Redis中查到数据
        if (dtoList != null) {
            return dtoList;
        }

        //2.从Redis中没有查到数据，到数据库中查询

        //条件查询器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //根据传进来的categoryId查询
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //只查询状态为1的菜品（启售菜品）
        queryWrapper.eq(Dish::getStatus, 1);
        //简单排下序，其实也没啥太大作用
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //获取查询到的结果作为返回值
        List<Dish> list = dishMapper.selectList(queryWrapper);

        //查询结果缺少菜品分类名称

        dtoList = list.stream()
                .map(item -> {
                    DishDto dishDto = new DishDto();
                    //将Dish对象拷贝给DishDto对象
                    BeanUtils.copyProperties(item, dishDto);
                    //获取categoryId对应的categoryName
                    Long categoryId = item.getCategoryId();
                    Category category = categoryService.getById(categoryId);
                    if (category != null) {
                        dishDto.setCategoryName(category.getName());
                    }
                    //当前菜品Id
                    Long dishId = item.getId();
                    LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
                    List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
                    dishDto.setFlavors(dishFlavorList);
                    return dishDto;
                }).collect(Collectors.toList());
        //将数据库查询到的数据放入Redis,做缓存
        redisTemplate.opsForValue().set(key, dtoList, 60, TimeUnit.MINUTES);
        return dtoList;
    }
}



