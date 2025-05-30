package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;

import java.util.List;

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

    /**
     *
     * 批量删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);
}
