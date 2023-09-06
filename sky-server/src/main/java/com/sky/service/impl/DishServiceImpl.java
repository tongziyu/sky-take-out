package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorsMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/6 18:22
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private FlavorsMapper flavorsMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 保存菜品和口味
     * 同时操作两张表需要使用到 事务!!
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveDishAndFlavor(DishDTO dishDTO) {
        // 先保存菜品
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.insert(dish);

        log.info("回传的菜品 id : {}",dish.getId());

        // 保存口味,批量插入
        List<DishFlavor> flavors = dishDTO.getFlavors();

        if (flavors.size() > 0 && flavors != null){
            // 设置口味对应的菜品id
            flavors.forEach(flavor ->
                    flavor.setDishId(dish.getId())
                    );

            flavorsMapper.insertBatch(flavors);
        }


    }

    /**
     * 分页查询,顺便查询出来 种类名称
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询数据:{}",dishPageQueryDTO);

        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        List<DishVO> dishVOS = dishMapper.pageQuery(dishPageQueryDTO);

        log.info("菜品分页查询出来的数据:{}",dishVOS);

        PageInfo pageInfo = new PageInfo(dishVOS);

        PageResult pageResult = new PageResult();

        pageResult.setTotal(pageInfo.getTotal());

        pageResult.setRecords(pageInfo.getList());


        return Result.success(pageResult);
    }

    /**
     * 批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {

        log.info("要删除的菜品 id: {}",ids);

        // 判断菜品是否是起售状态,如果是,就不能删除
        for (Long id: ids) {
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == 1){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断菜品是否在套餐内
        List<Long> longs = setmealDishMapper.selectByDishIds(ids);

        if (longs != null && longs.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        // 删除菜品
        if (ids != null && ids.size() > 0){
            dishMapper.deleteByIds(ids);

        }
        // 删除口味,不需要判断 直接删除
        flavorsMapper.deleteByIds(ids);
    }

    @Override
    public DishVO getDishByIdWithFlavor(Long id) {
        Dish dish = dishMapper.selectById(Long.valueOf(id));

        log.info("通过id查询出来的dish:{}",dish);

        DishVO dishVo = new DishVO();
        BeanUtils.copyProperties(dish,dishVo);

        List<DishFlavor> dishFlavors = flavorsMapper.selectById(id);

        dishVo.setFlavors(dishFlavors);

        return dishVo;

    }
}
