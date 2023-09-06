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

    @GetMapping("/page")
    @ApiOperation("菜品分类查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info(String.valueOf(dishPageQueryDTO));

        Result<PageResult> pageResultResult = dishService.pageQuery(dishPageQueryDTO);

        return pageResultResult;
    }

    @DeleteMapping
    @ApiOperation("批量删除")
    public Result deleteByIds(@RequestParam("ids") List<Long> ids){
        log.info("需要删除的id:{}",ids);

        dishService.deleteByIds(ids);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getDishByIdWithFlavor(@PathVariable("id") Long id){
        log.info("要查询菜品的id :{}",id);
        DishVO dishById = dishService.getDishByIdWithFlavor(id);

        log.info("查询出来的菜品 {}",dishById);
        return Result.success(dishById);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDishAndFlavor(@RequestBody DishDTO dishDTO){
        log.info("修改菜品的数据:{}",dishDTO);
        dishService.updateDishAndFlavor(dishDTO);

        return Result.success();
    }

    @ApiOperation("菜品起售停售")
    @PostMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status,
                               @RequestParam("id") Long id){
        dishService.updateStatus(id,status);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询商品")
    public Result<List<Dish>> getListByCategoryId(Long categoryId){
        List<Dish> dishes = dishService.selectByCategoryId(categoryId);



        return Result.success(dishes);
    }


}
