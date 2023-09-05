package com.sky.service.impl;

import com.aliyuncs.ecs.model.v20140526.DescribeEipMonitorDataResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
        // 将前端传来的数据进行md5加密,对加密后的密码,和数据库里的密码进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        log.info("前端的密码 md5值:" + password);
        log.info("数据库查询的密码 md5值:" + employee.getPassword());

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


    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 将employeeDTO对象转换成Employee对象
        Employee employee = new Employee();

        BeanUtils.copyProperties(employeeDTO,employee);

        log.info("employeeDTO对象:" + employeeDTO);

        // 配置员工默认密码 md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 配置员工的创建时间
        employee.setCreateTime(LocalDateTime.now());

        // 配置员工的最后修改时间
        employee.setUpdateTime(LocalDateTime.now());

        // 配置员工的状态
        employee.setStatus(StatusConstant.ENABLE);

        // 配置员工的创建人和修改人
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());


        log.info("employee对象:" + employee);


        // 将对象保存进数据库
        Integer insert = employeeMapper.insert(employee);



    }

    /**
     * 分页查询的业务逻辑
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public Result getPage(EmployeePageQueryDTO employeePageQueryDTO) {
        // 开启分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        List<Employee> employees = employeeMapper.selectPage(employeePageQueryDTO);

        PageInfo pageInfo = new PageInfo(employees);

        // 创建pageResult对象,封装数据!
        PageResult pageResult = new PageResult();

        pageResult.setTotal(pageInfo.getTotal());

        pageResult.setRecords(pageInfo.getList());

        log.info("分页查询->总记录条数:{} 数据:{}",pageResult.getTotal(),pageResult.getRecords());

        return Result.success(pageResult);
    }
}
