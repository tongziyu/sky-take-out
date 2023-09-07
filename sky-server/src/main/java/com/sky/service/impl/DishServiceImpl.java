package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorsMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Autowired
    private SetmealMapper setmealMapper;



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
        // select id from setmeal_dish where dish_id = #{ids}
        List<Long> setmealIdByDishIds = setmealDishMapper.selectByDishIds(ids);

        log.info("菜品相关联的套餐:{}",ids);
        log.info("longs.size = {}",setmealIdByDishIds.size());

        if (setmealIdByDishIds != null && setmealIdByDishIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品
        if (ids != null && ids.size() > 0){
            dishMapper.deleteByIds(ids);

        }
        // 删除口味,不需要判断 直接删除
        flavorsMapper.deleteByDishIds(ids);
    }

    /**
     * 通过id获得菜品和口味
     * @param id
     * @return
     */
    @Override
    public DishVO getDishByIdWithFlavor(Long id) {
        Dish dish = dishMapper.selectById(Long.valueOf(id));

        log.info("通过id查询出来的dish:{}",dish);

        DishVO dishVo = new DishVO();
        BeanUtils.copyProperties(dish,dishVo);

        List<DishFlavor> dishFlavors = flavorsMapper.selectByDishId(id);

        dishVo.setFlavors(dishFlavors);

        return dishVo;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateDishAndFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO,dish);

        // 修改菜品
        dishMapper.update(dish);


        // 修改口味
        // 直接将对应菜品id的口味全部删除,后重新插入

        flavorsMapper.deleteByDishId(dishDTO.getId());


        if (dishDTO.getFlavors() == null || dishDTO.getFlavors().size() == 0){

            return;
        }

        List<DishFlavor> flavors = dishDTO.getFlavors();

        for(DishFlavor dishFlavor:flavors){

            dishFlavor.setDishId(dish.getId());
        }

        log.info("要添加的口味:{}",flavors);

        flavorsMapper.insertBatch(flavors);
    }

    /**
     * 修改菜品状态
     * @param id
     * @param status
     */
    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {

        // 修改状态
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);

        // 判断如果停售状态,则将对应的套餐也停用
        if (status == StatusConstant.DISABLE){
            // 通过Dish id获取到 关系表中的关联了该dish的 套餐  不止有一个
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            List<Long> setmealIds = setmealDishMapper.selectByDishIds(ids);
            for (Long setmealId : setmealIds){
                Setmeal setmeal = new Setmeal();

                setmeal.setId(setmealId);

                setmeal.setStatus(StatusConstant.DISABLE);

                // 获取到关联的套餐id后,修改status
                setmealMapper.update(setmeal);
            }
        }
    }

    @Override
    public List<Dish> selectByCategoryId(Long categoryId) {
        // 后台使用动态sql
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);


        List<Dish> dishes = dishMapper.selectLikeNameCategoryStatus(dish);
        return dishes;
    }


}
