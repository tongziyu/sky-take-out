package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/12 02:03
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;


    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add( ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();

        // 复制属性给shoppingCart对象
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);

        // 从本地线程变量中获取userId;
        Long userId = BaseContext.getCurrentId();

        shoppingCart.setUserId(userId);

        log.info("shoppingCart对象:{}",shoppingCart);

        /*
        逻辑: 首先去查询购物车中有没有这条数据,如果有就直接数量加+1
                如果没有这条数据,则新建一条数据
         */
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 判断 list 如果不是空的,则进行数量+1
        if (list != null && list.size() >0){

            ShoppingCart shoppingCart1 = list.get(0);

            Integer number = shoppingCart1.getNumber();

            shoppingCart1.setNumber(number + 1);

            // 修改数据库表的数据
            shoppingCartMapper.update(shoppingCart1);
        }

        // 如果list中没有数据,则表示需要直接添加一条数据
        if (list == null || list.size() == 0){

            // 判断是否是dish
            if (shoppingCart.getDishId() != null){

                Dish dish = dishMapper.selectById(shoppingCart.getDishId());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setName(dish.getName());
                log.info("查询出来的dish数据{}",dish);
            }else{
                Setmeal setmeal = setmealMapper.selectById(shoppingCart.getSetmealId());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setName(setmeal.getName());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            log.info("需要插入到shoppingcart表中的数据:{}",shoppingCart);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 查看购物车,根据id查询购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void delete() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteAllByUserId(userId);
    }
}
