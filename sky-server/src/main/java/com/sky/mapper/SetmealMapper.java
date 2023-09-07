package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);


    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);


    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    Setmeal selectById(Long id);

    List<SetmealVO> selectLikeCategoryIdNameStatus(Setmeal setmeal);


    void deleteBatch(List<Long> ids);
}
