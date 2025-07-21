package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作套餐和菜品关系表的mapper
 */
@Mapper
public interface SetmealDishMapper {
    /**
     * 获取套餐id根据菜品id
     * @param ids
     * @return
     */
    List<Long> getSetmealIdsByDishId(List<Long> ids);

    /**
     * 批量插入关联关系
     * @param list
     */
    void insertBatch(List<SetmealDish> list);

    /**
     * 获取关联关系
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getSetmeals(Long id);

    /**
     * 批量删除套餐菜品关联关系
     * @param ids
     */
    void deleteByIds(List<Long> ids);
}
