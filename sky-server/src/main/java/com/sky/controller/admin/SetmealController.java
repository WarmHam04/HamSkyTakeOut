package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishService dishService;

    //新增菜品
    @PostMapping
    @ApiOperation("新增套餐")
    public Result saveSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐,{}", setmealDTO);

        setmealService.save(setmealDTO);
        return Result.success();
    }

    //分页查询套餐
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> getSetmealPage(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("分页查询{}", setmealPageQueryDTO);
        PageResult page = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(page);
    }

    //批量删除套餐
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result deleteBatch(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}",ids);

        setmealService.deleteBatch(ids);
        return Result.success();
    }

    //查询套餐，为修改套餐栏单里回显数据做准备
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        SetmealVO so = setmealService.getBySetmealId(id);
        return Result.success(so);
    }


    //修改套餐
    @PutMapping
    @ApiOperation("修改套餐")
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐{}", setmealDTO);

        setmealService.update(setmealDTO);
        return Result.success();
    }

    //修改套餐状态
    @PostMapping("status/{status}")
    @ApiOperation("修改套餐出售状态")
    public Result updateSetmealStatus(@PathVariable Integer status,Long id) {
        log.info("修改套餐的状态{}",status);
        setmealService.updateStatus(status,id);
        return Result.success();
    }

}
