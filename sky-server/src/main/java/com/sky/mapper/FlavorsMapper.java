package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 口味相关的接口
 * @Author: Ian
 * @Date: 2023/9/6 18:28
 */
@Mapper
public interface FlavorsMapper {

    /**
     * 批量插入菜品的口味
     * @param flavors
     */


    void insertBatch(List<DishFlavor> flavors);

    void deleteByIds(List<Long> ids);


    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> selectById(Long dishId);
}
