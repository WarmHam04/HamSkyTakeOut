package com.sky.controller.admin;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    public DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品,{}", dishDTO);
        //删除所有相关缓存
        String keys = "dish_"+dishDTO.getCategoryId();
        cleanCache(keys);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    //接口文档的query数据不需要json，所以不需要@RequestBody 注解
    public Result<PageResult> page(DishPageQueryDTO dishDTO) {
        log.info("分页查询{}", dishDTO);
        PageResult page = dishService.pageQuery(dishDTO);
        return Result.success(page);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteDish(@RequestParam List<Long> ids){
        log.info("批量删除菜品：{}",ids);
        //一次性删除所有相关缓存
        String pattern = "dish_*";
        cleanCache(pattern);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    //查询菜品数据及口味数据，为修改菜品栏单里回显数据做准备
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        DishVO dv = dishService.getByDishIdWithFlavor(id);
        return Result.success(dv);
    }

    //修改菜品
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品{}", dishDTO);
        //删除有关缓存即可
        String keys = "dish_*";
        cleanCache(keys);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

    }

    @PostMapping("status/{status}")
    @ApiOperation("起售商品或停售商品")
    public Result<String> status(@PathVariable Integer status,Long id){
        log.info("修改菜品的状态{}",status);
        String pattern = "dish_*";
        cleanCache(pattern);
        dishService.startOrStop(status,id);
        return Result.success();
    }
}
