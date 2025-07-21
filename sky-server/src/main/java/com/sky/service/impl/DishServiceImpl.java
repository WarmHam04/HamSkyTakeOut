package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;



    /**
     * 插入菜品数据和口味数据
     * @param dishDTO
     */
    @Transient//全成功全失败注解
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //插入菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);

        //获取主键值生成的dishId
        Long dishId = dish.getId();

        //插入菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }


    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dto) {
        //使用pagehelper来实现分页查询功能
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<DishVO> vo = dishMapper.pageQuery(dto);
        return new PageResult(vo.getTotal(), vo.getResult());
    }


    @Transactional//事务注解，确保代码的一致性
    /**
     *
     * 批量删除菜品
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {

        //判断该菜品是否仍在商品架上，即判断菜品状态，如果菜品状态为1，不可删除
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);

            if(dish.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断菜品是否关联于套餐里，如果关联，不可删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(ids);
        if(setmealIds != null && setmealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

//        //删除菜品
//        for(Long id : ids){
//            dishMapper.deleteById(id);
//            //删除菜品的口味
//            dishFlavorMapper.deleteFlavorByDishId(id);
//        }

        //优化代码，减少sql语句发生量
        //sql ： delete from dish where id in (?,?,?)
        //删除菜品
        dishMapper.deleteByIds(ids);

        //删除菜品的关联口味
        //优化代码，减少sql语句发生量
        //sql ： delete from dish_flavor where dish_id in (?,?,?)
        dishFlavorMapper.deleteFlavorByDishIds(ids);

    }

    /**
     * 根据菜品id查询菜品数据和口味数据
     * @param id
     * @return
     */
    @Override
    public DishVO getByDishIdWithFlavor(Long id) {
        //获取口味数据
        List<DishFlavor> list = dishFlavorMapper.getById(id);
        Dish dish = dishMapper.getById(id);
        //将dish数据复制到dishvo中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(list);
        return dishVO;
    }

    /**
     * 修改菜品数据
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //设置菜品的基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.update(dish);

        //获取dishId
        Long dishId = dish.getId();

        //设置菜品的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
        }
        dishFlavorMapper.insertBatch(flavors);

    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {


        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getById(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;

    }

    /**
     * 修改菜品售卖状态
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder().id(id).status(status).build();
        dishMapper.update(dish);

    }
}
