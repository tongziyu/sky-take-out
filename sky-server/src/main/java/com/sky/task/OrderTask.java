package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/13 21:12
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    /**
     * 每分钟去查一遍数据库,将未付款切超过15分钟的订单取消
     */
    @Scheduled(cron = "0 */1 * * * ?")
    //@Scheduled(cron = "0/5 * * * * ? ")
    public void updatePayTimeOutOrder(){
        log.info("付款超时任务执行!!! time:{}",LocalDateTime.now());
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(-15);

        List<Orders> orders = ordersMapper.selectTimeOutOrder(Orders.PENDING_PAYMENT,localDateTime);
        if (orders != null && orders.size() >0) {
            for (Orders od : orders) {
                od.setStatus(Orders.CANCELLED);
                od.setCancelTime(LocalDateTime.now());
                od.setCancelReason("订单超时,自动取消");
                ordersMapper.update(od);
            }
        }
    }

    /**
     * 处理昨天的派送订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "1/5 * * * * ? ")
    public void updateProcessDeliveryOrder(){
        log.info("派送超时任务执行!!! time:{}",LocalDateTime.now());



        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(60);

        List<Orders> orders = ordersMapper.selectTimeOutOrder(Orders.DELIVERY_IN_PROGRESS,localDateTime);

        if (orders != null && orders.size() >0){
            for (Orders od: orders){
                od.setStatus(Orders.COMPLETED);
                ordersMapper.update(od);
            }
        }
    }
}
