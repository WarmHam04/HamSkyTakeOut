package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }


    @PostMapping()
    @ApiOperation("添加员工")
    public Result insert(@RequestBody EmployeeDTO employeeDTO) {
        //{}是占位符，运行后会将employeeDTO的数据放到{}中来
        log.info("新增员工，{}", employeeDTO);
        employeeService.insert(employeeDTO);
        return Result.success();
    }


    /**
     *
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询员工")
    //无需json格式，不需要@RequestBody
    public Result<PageResult> query(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("分页查询员工{}", employeePageQueryDTO);
        //pageResult有两个属性，一个是总条数，另一个是数据列表
        PageResult pageResult = employeeService.query(employeePageQueryDTO);
        //封装Result
        return Result.success(pageResult);
    }


    @PostMapping("/status/{status}")
    @ApiOperation("禁用员工或者启用员工账号")
    //路径参数 status 使用@PathVariable
    public Result status(@PathVariable Integer status,Long id) {
        log.info("禁用员工或者启用员工账号,{},{}", status, id);
        employeeService.updateStatus(status,id);
        return Result.success();
    }


    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    //路径参数
    public Result<Employee> getId(@PathVariable Long id) {
        log.info("根据id查询员工信息,{}",id);
        Employee emp = employeeService.getById(id);
        return Result.success(emp);
    }

    /**
     * "修改员工信息"
     * @param employeeDTO
     * @return
     */
    @PutMapping("/update")
    @ApiOperation("修改员工信息")
    public Result uodate(@RequestBody EmployeeDTO employeeDTO) {
        log.info("修改员工信息,{}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }

}
