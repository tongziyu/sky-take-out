package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.FlavorsMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/7 01:14
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private FlavorsMapper flavorsMapper;

    /**
     * 添加套餐的方法
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {

        // 插入套餐,并且获得回显id
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO,setmeal);



        log.info("需要插入的套餐:{}",setmeal);

        setmealMapper.insert(setmeal);



        // 插入套餐份数
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 将套餐id放入关系对象中
        for (SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmeal.getId());
        }

        log.info("dish数据:{}", setmealDishes);

        // 在关系表中保存 套餐和份数的关系
        setmealDishMapper.insert(setmealDishes);

    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        Setmeal setmeal = new Setmeal();

        // 开启分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        BeanUtils.copyProperties(setmealPageQueryDTO,setmeal);


        log.info("模糊查询的数据:{}",setmeal);

        List<SetmealVO> setmeals = setmealMapper.selectLikeCategoryIdNameStatus(setmeal);


        PageInfo pageInfo = new PageInfo(setmeals);

        log.info("模糊查询出来的数据:{}",pageInfo.getList());

        PageResult pageResult = new PageResult();

        pageResult.setRecords(pageInfo.getList());
        pageResult.setTotal(pageInfo.getTotal());

        return pageResult;
    }

    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        // 根据ids列表,删除套餐

        if (ids == null && ids.size() == 0){
            throw new DeletionNotAllowedException(MessageConstant.SELECT_IS_EMPTY_SETMEAL);
        }


        // 判断该套餐 是否是起售状态,如果是则不可以删除
        if (ids != null && ids.size() > 0){
            // 循环查询,如果查询出来有一个套餐是起售状态,直接抛出异常
            for (Long id : ids){
                Setmeal setmeal = setmealMapper.selectById(id);

                if (setmeal.getStatus() == 1){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
                }
            }

            // 批量删除setmeal
            setmealMapper.deleteBatch(ids);
        }



        // 删除完套餐,删掉 setmeal_dish 表中关联的数据
        setmealDishMapper.deleteBatch(ids);




    }
}
