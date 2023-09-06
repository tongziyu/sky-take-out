package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorsMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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
}
