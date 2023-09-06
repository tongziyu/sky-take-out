package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/7 01:12
 */

@RestController
@Slf4j
@Api("套餐相关接口")
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;


    @PostMapping
    @ApiOperation("新增套餐")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐内容:{}",setmealDTO);

        setmealService.addSetmeal(setmealDTO);

        return Result.success();
    }

}
