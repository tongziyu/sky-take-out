package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;

public interface DishService {
    void saveDishAndFlavor(DishDTO dishDTO);

    Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO);
}
