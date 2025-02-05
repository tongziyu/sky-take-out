package com.sky.service;


import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {



    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @return
     */
    List<ShoppingCart> list();


    /**
     * 清空购物车
     */
    void delete();


    /**
     * 购物车商品-1
     * @param shoppingCartDTO
     */
    void sub(ShoppingCartDTO shoppingCartDTO);

}
