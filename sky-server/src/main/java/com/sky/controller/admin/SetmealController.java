package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐内容:{}",setmealDTO);

        setmealService.addSetmeal(setmealDTO);

        return Result.success();
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page( SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页模糊查询的数据:{}",setmealPageQueryDTO);

        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);

        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result deleteByIds(@RequestParam("ids")List<Long> ids){
        setmealService.deleteByIds(ids);
        log.info("批量删除套餐的id:{}",ids);

        return Result.success();
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("更新套餐")
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO){

        setmealService.update(setmealDTO);


        return Result.success();
    }


    @GetMapping("/{id}")
    @ApiOperation("通过id查询套餐")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id){

        Result result = setmealService.getSetmealById(id);

        return result;
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售,停售")
    public Result updateStatus(@PathVariable("status")Integer status,@RequestParam("id") Long id){
        setmealService.updateStatusById(status,id);
        return Result.success();
    }

}
