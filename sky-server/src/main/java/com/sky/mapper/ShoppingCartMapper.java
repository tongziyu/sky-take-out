package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> list(ShoppingCart shoppingCart);


    void update(ShoppingCart shoppingCart);


    void insert(ShoppingCart shoppingCart);


    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteAllByUserId(Long userId);

    /**
     * 删除购物车
     * @param shoppingCart
     */
    void sub(ShoppingCart shoppingCart);

    /**
     * 根据id删除一条购物车记录
     * @param shoppingCart
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteOne(ShoppingCart shoppingCart);
}
