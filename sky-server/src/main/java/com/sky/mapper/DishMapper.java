package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 保存菜品,并将自增id保存进对象
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);


    /**
     * 分页查询
     * @param dishPageQueryDTO
     * @return
     */
    List<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);



    Dish selectById(Long id);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);
}
