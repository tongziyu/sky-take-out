package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Target;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/6 18:21
 */
@RestController
@Api(tags = "菜品接口")
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 保存菜品和口味
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("保存菜品和口味")
    public Result saveDishAndFlavor(@RequestBody DishDTO dishDTO){
        log.info("保存的菜品信息:{}",dishDTO);

        dishService.saveDishAndFlavor(dishDTO);

        return Result.success();
    }

}
