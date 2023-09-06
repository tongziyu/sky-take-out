package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorsMapper;
import com.sky.service.DishService;
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
}
