package com.sky.mapper;

import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    List<Orders> selectAllDynamic(OrdersPageQueryDTO orders);

    @Select("select * from orders")
    List<Orders> selectAll();

    @Select("select count(status) from orders where status = #{status}")
    Integer selectCountByStatus(Integer status);

    @Update("update orders set status = #{status} where id = #{id}")
    void updateStatusById(OrdersConfirmDTO ordersConfirmDTO);

    void updateStatusRejectById(Orders orders);

    @Update("update orders set status = #{status} where id = #{id}")
    void updateStatusDelivery(Orders od);


    @Select("select * from orders where status = #{status} and order_time < #{timeOut}")
    List<Orders> selectTimeOutOrder(@Param("status") Integer status, @Param("timeOut") LocalDateTime timeOut);

    /**
     * 通过订单号 直接将订单状态改成 已付款
     * @param ordersPaymentDTO
     */
    @Update("update orders set status = 2 where number = #{orderNumber}")
    void updateStatusByNumberToBeConfirmed(OrdersPaymentDTO ordersPaymentDTO);

    Double sumByMap(Map map);

    Integer selectOrderCountByMap(Map map);
    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
