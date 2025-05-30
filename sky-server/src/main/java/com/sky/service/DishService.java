package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;

public interface DishService {
    /**
     * 插入菜品数据
     * @param dishDTO
     */
     void saveWithFlavor(DishDTO dishDTO);

    /**
     *
     * 分页查询
     * @param dishDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishDTO);
}
