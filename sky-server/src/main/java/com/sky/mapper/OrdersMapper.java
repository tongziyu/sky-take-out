package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper {

    /**
     * 插入订单数据,并返回主键
     * @param orders
     */
    void insert(Orders orders);


}
