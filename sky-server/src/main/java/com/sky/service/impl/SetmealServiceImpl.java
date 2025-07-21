package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 添加套餐
     * @param setmealDTO
     */
    @Override
    public void save(SetmealDTO setmealDTO) {
        //插入套餐
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.insert(setmeal);

        //获取套餐id
        Long id = setmeal.getId();
        //插入套餐菜品关联
        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        if(list!=null&&list.size()>0){
            for (SetmealDish dish : list) {
                dish.setSetmealId(id);
            }setmealDishMapper.insertBatch(list);
        }
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //使用pagehelper来实现分页查询功能
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> vo = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(vo.getTotal(),vo.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断套餐状态
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);

            if(setmeal.getStatus()==StatusConstant.ENABLE)throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }

        //删除套餐
        setmealMapper.deleteByIds(ids);

        //删除套餐和菜品的关联关系
        setmealDishMapper.deleteByIds(ids);


    }

    /**
     * 根据套餐id回显套餐数据和菜品数据
     * @param id
     * @return
     */
    @Override
    public SetmealVO getBySetmealId(Long id) {
        //获取关系列表数据
        List<SetmealDish> list = setmealDishMapper.getSetmeals(id);
        Setmeal setmeal = setmealMapper.getById(id);
        //输出SetmealVO
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(list);
        return setmealVO;

    }

    /**
     * 修改套餐数据
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        //设置
        Long id = setmeal.getId();

        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        if(list!=null&&list.size()>0){
            for (SetmealDish dish : list) {
                dish.setSetmealId(id);
            }
        }setmealDishMapper.insertBatch(list);
    }

    /**
     * 修改套餐状态
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id) {
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal);

    }
}
