package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());//将前端接收到的密码转化为md5加密后的密文
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void insert(EmployeeDTO employeeDTO) {
        //直接复制
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        employee.setStatus(StatusConstant.ENABLE);



        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录人id
        //HAVE DONE 后期改为当前登录人的id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);



    }


    /**
     *分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult query(EmployeePageQueryDTO employeePageQueryDTO) {
        //使用分页查询插件
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.query(employeePageQueryDTO);


        long total = page.getTotal();
        List<Employee> record = page.getResult();

        //封装PageResult
        return new PageResult(total, record);


    }

    /**
     * 禁用员工或者启用员工账号
     * @param status
     * @param id
     */
    @Override
    public void updateStatus(Integer status, Long id) {
//        Employee employee = new Employee();
//        employee.setId(id);
//        employee.setStatus(status);

        //builder写法
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                        .build();

        employeeMapper.updateStatus(employee);

    }

    @Override
    public Employee getById(Long id) {
        Employee emp = employeeMapper.getById(id);
        emp.setPassword("*********");
        return emp;
    }

    /**
     * "修改员工信息"
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee emp = new Employee();
        BeanUtils.copyProperties(employeeDTO, emp);
        emp.setUpdateTime(LocalDateTime.now());
        emp.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.updateStatus(emp);
    }


}
