package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/8 23:06
 */
@Api(tags = "店铺相关接口")
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {
    public static final String shopStatus = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation("设置店铺状态")
    @PutMapping("{status}")
    public Result setStatus(@PathVariable Integer status){
        redisTemplate.opsForValue().set(shopStatus,status);
        log.info("将要设置的店铺状态:{}",status);
        return Result.success();

    }

    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer shopStatus1 = (Integer)redisTemplate.opsForValue().get("SHOP_STATUS");

        log.info("当前店铺状态:{}",shopStatus1);
        return Result.success(shopStatus1);

    }



}
