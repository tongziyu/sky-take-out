package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.result.Result;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;

public interface SetmealService {
    /**
     * 添加套餐的方法
     * @param setmealDTO
     */
    void addSetmeal(SetmealDTO setmealDTO);


}
