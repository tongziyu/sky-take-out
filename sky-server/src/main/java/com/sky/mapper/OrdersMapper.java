package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrdersMapper {

    /**
     * 插入订单数据,并返回主键
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据userId查询历史订单,如果status不为空 则加上status条件
     * @param userId
     * @param status
     * @return
     */
    List<Orders> selectHistoryOrder(@Param("userId") Long userId,
                                    @Param("status") Integer status);


    @Select("select * from orders where id = #{id}")
    Orders getOrderById(Long id);


    void updateStatusCancelById(Orders orders);

    @Select("select * from orders where id = #{id}")
    Orders selectOrderById(Long id);
}
