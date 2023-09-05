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
@Api(tags = "员工相关接口")
@RestController
@RequestMapping("/admin/employee")
@Slf4j
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
    @ApiOperation("登录功能")
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
    @ApiOperation("退出功能")
    public Result<String> logout() {
        return Result.success();
    }


    @ApiOperation("添加用户")
    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO){

        employeeService.save(employeeDTO);

        return Result.success();

    }

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> getPage(EmployeePageQueryDTO employeePageQueryDTO){

        log.info("分页查询->  page:{}  size:{} ",employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());

        Result r = employeeService.getPage(employeePageQueryDTO);

        return  r;
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工状态")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("修改员工状态-> 员工id:{} 员工status:{}",id,status);
        employeeService.startOrStop(status,id);

        return Result.success();
    }



    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> getEmployeeById(@PathVariable Long id){
        Result<Employee> result = employeeService.getEmployeeById(id);
        log.info("查询到的数据: {}",result);

        return result;
    }

    @PutMapping
    @ApiOperation("修改员工信息")
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO){
        log.info("需要修改的employee对象: {}",employeeDTO);

        Result result = employeeService.updateEmployee(employeeDTO);
        return result;

    }























}
