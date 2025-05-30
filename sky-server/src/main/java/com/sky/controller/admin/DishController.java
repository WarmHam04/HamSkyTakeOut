package com.sky.controller.admin;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    public DishService dishService;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品,{}", dishDTO);
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

    //批量删除，前端query传回数据形式：1,2,3,4......
    //使用spring mvc框架的@RequestParam注解来动态解析为List<Long>
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteBatch(@RequestParam List<Long> ids) {
        log.info("批量删除菜品:{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }
}
