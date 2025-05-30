package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 操作套餐和菜品关系表的mapper
 */
@Mapper
public interface SetmealDishMapper {
    List<Long> getSetmealIdsByDishId(List<Long> ids);
}
