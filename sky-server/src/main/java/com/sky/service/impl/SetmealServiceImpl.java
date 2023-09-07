package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
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

import java.awt.event.FocusEvent;
import java.util.ArrayList;
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

    @Autowired
    private DishMapper dishMapper;

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

    /**
     * 修改套餐数据
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO,setmeal);

        log.info("需要修改的套餐数据:{}",setmeal);
        /* 思路: 修改套餐
                1. 修改套餐信息
                2. 直接删除对应的setmeal_dish 表里面的数据
                3. 直接插入setmeal_dish 表里面的数据
         */

        // 1.修改套餐信息
        setmealMapper.update(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 2.批量删除setmeal_dish表里面的数据

        List<Long> setmealIds = new ArrayList<>();

        for (SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmeal.getId());
            setmealIds.add(setmealDish.getSetmealId());
            log.info("需要删除的setmeal_id为:{}",setmealDish.getSetmealId());
        }
        setmealDishMapper.deleteBatch(setmealIds);

        // 3.批量插入setmeal_dish表里面的数据

        setmealDishMapper.insert(setmealDishes);

        // 4.修改套餐id
        return;
    }

    /**
     * 根据id查询出来菜品信息,联合查询,查出来dish信息 和 种类名称
     * 需要连三张表
     * @param id
     * @return
     */
    @Override
    public Result<SetmealVO> getSetmealById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);

        log.info("通过id查询出来的数据:{}",setmeal);

        // 通过 setmeal 的id将setmealDish 表中的数据查询出来
        List<SetmealDish> setmealDishes = setmealDishMapper.selectBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();

        BeanUtils.copyProperties(setmeal,setmealVO);

        setmealVO.setSetmealDishes(setmealDishes);


        return Result.success(setmealVO);
    }

    /**
     * 修改套餐的状态
     *  - 如果套餐内有菜品的状态是停售状态,则起售套餐是不能执行的
     * @param status
     * @param id
     */
    @Override
    @Transactional
    public void updateStatusById(Integer status, Long id) {

        // 在修改套餐起售停售的时候,判断套餐内的菜品有没有停售,如果有停售,则无法开启 起售

        if (status == StatusConstant.ENABLE){
            // 通过套餐的id 查询出来dish 的id
            List<Long> dishIds =  setmealDishMapper.selectDishIdsBySetmealId(id);

            // 通过dish的id查询到status,如果是0则直接抛出异常 表示 有菜品是停售,套餐不能起售
            for(Long dishId: dishIds){
                Dish dish = dishMapper.selectById(dishId);
                if (dish.getStatus() == StatusConstant.DISABLE){
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }

        // 修改套餐的起售停售
        setmealMapper.updateStatusById(status,id);

    }
}
