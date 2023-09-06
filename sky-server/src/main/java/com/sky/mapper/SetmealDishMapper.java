package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/6 21:28
 */

@Mapper
public interface SetmealDishMapper {



    List<Long> selectByDishIds(List<Long> ids);
}
