package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.*;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);


    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 历史订单查询,分页查询
     * @return
     */
    PageResult getHistoryOrdersPage(Integer page,Integer pageSize, Integer status);

    OrderVO getOrderDetailByOrderId(Long id);

    void cancelById(Long id);

    /**
     * 再来一单
     * @param id
     */
    void repetitionOrder(Long id);

    /**
     * 查询所有订单,分页查询,按条件查询
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO getStatistics();


    /**
     * 修改订单状态为已经接单
     */
    void updateStatusById(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 修改订单状态为拒绝,并添加拒绝原因
     */
    void updateStatusRejectById(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    void updateStatusCancel(OrdersCancelDTO ordersCancelDTO);

    void updateStatusDelivery(Long id);

    void updateStatusComplete(Long id);
    /**
     * 通过订单号 直接将订单状态改成 已付款
     */
    void updateStatusByNumber(OrdersPaymentDTO ordersPaymentDTO);

    void sendWsComeOrder(String outTradeNo);

    /**
     * 催单
     * @param id
     */
    void reminder(Long id);

    /**
     * 统计营业额
     * @param begin
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    /**
     * 查询指定时间区间内的销量前十
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);
}
