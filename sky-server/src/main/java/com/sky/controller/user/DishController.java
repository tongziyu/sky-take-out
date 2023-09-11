package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        /* 首先先去redis里面去查,如果再redis里面查到了,就直接返回
            如果没有查到则进行数据库查询,然后将数据库查询出来的 保存进redis

        */
        // 构造key  dish_categoryId
        String key = "dish_" + categoryId;

        // 当初保存进redis的数据类型是啥,取出来的时候就是啥,直接强转
        List<DishVO> value = (List<DishVO>) redisTemplate.opsForValue().get(key);

        // 如果在redis中查询到了,则将查询的数据直接返回给前端
        if (value != null && value.size() > 0){
            return Result.success(value);
        }

        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        List<DishVO> list = dishService.listWithFlavor(dish);

        // 将从数据库里面查询到的数据 保存进redis
        // 保存类型是 List<DishVo>
        redisTemplate.opsForValue().set(key,list);

        return Result.success(list);
    }

}
