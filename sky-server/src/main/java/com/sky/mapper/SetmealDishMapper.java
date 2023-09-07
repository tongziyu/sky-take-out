package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/6 21:28
 */

@Mapper
public interface SetmealDishMapper {



    //List<Long> selectByDishIds(List<Long> ids);

    void insert(List<SetmealDish> setmealDishes);

    void deleteBatch(List<Long> setmealIds);

    List<Long> selectByDishIds(List<Long> ids);

    List<SetmealDish> selectBySetmealId(Long id);
}
