package com.sky.service;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    void saveDishAndFlavor(DishDTO dishDTO);

    Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteByIds(List<Long> ids);

    DishVO getDishByIdWithFlavor(Long id);


    void updateDishAndFlavor(DishDTO dishDTO);

    void updateStatus(Long id, Integer status);


    List<Dish> selectByCategoryId(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
