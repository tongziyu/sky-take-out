package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.spi.BeanTool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/12 15:44
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;


    /**
     * 用户订单提交
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        /*
        业务异常判断:
            - 判断购物车里面是否有东西
            - 判断地址信息是否有效
        思路:
            - 在订单表中添加一条数据
            - 在订单详情表中添加多条数据
            - 删除对应UserId的购物车数据
            - 封装一个OrderSubmitVo数据返回
         */
        log.info("orderSubmitDto:{}",ordersSubmitDTO);
        // 判断购物车里面是否有东西
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        log.info("需要查询购物车的UserId:{}",shoppingCart.getUserId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list == null || list.size() == 0 ){
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 判断地址信息是否有效
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        if (addressBook == null){
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 在订单表中添加一条数据
        Orders orders = new Orders();

        BeanUtils.copyProperties(ordersSubmitDTO,orders);

        orders.setOrderTime(LocalDateTime.now());

        orders.setPayStatus(Orders.UN_PAID);

        // 订单号使用时间戳来代表
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        orders.setPhone(addressBook.getPhone());

        orders.setStatus(Orders.PENDING_PAYMENT);

        orders.setUserId(BaseContext.getCurrentId());

        orders.setConsignee(addressBook.getConsignee());

        orders.setAddress(addressBook.getDetail());


        log.info("需要插入到orders表中的数据:{}",orders);

        ordersMapper.insert(orders);

        // 在订单详情表中添加多条数据
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (ShoppingCart shoppingCart1 : list){
            OrderDetail orderDetail = new OrderDetail();

            BeanUtils.copyProperties(shoppingCart1,orderDetail);

            log.info("orders的id:{}",orders.getId());

            orderDetail.setId(null);

            orderDetail.setOrderId(orders.getId());

            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);


        // 删除对应UserId的购物车数据

        shoppingCartMapper.deleteAllByUserId(BaseContext.getCurrentId());

        // 封装一个OrderSubmitVo数据返回
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();

        orderSubmitVO.setOrderTime(orders.getOrderTime());

        orderSubmitVO.setOrderAmount(orders.getAmount());

        orderSubmitVO.setOrderNumber(orders.getNumber());

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);

        //调用微信支付接口，生成预支付交易单
        /* JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );*/
        JSONObject jsonObject = new JSONObject();


        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.update(orders);
    }

    public void sendWsComeOrder(String outTradeNo){

        Orders ordersDB = ordersMapper.getByNumber(outTradeNo);

        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:" + outTradeNo);
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);

        log.info("webSocket 已经向前端发送数据!!");
    }



        @Override
    public PageResult getHistoryOrdersPage(Integer page,Integer pageSize, Integer status) {
        PageResult pageResult = new PageResult();

        log.info("page:{}, pageSize:{}, status:{}",page,pageSize,status);

        PageHelper.startPage(page,pageSize);

        // 查询订单
        Long userId = BaseContext.getCurrentId();

        // 通过userId查询出来所有的订单
        List<Orders> orders = ordersMapper.selectHistoryOrder(userId,status);


        PageInfo pageInfo = new PageInfo(orders);

        pageResult.setTotal(pageInfo.getTotal());

        List<OrderVO> list = new ArrayList<>();

        // 查询ordersDetail信息, order_id =

        if (orders != null && orders.size() > 0){

            for (Orders orders1 : orders){
                List<OrderDetail> orderDetails = orderDetailMapper.selectListByOrderId(orders1.getId());

                OrderVO orderVO = new OrderVO();

                BeanUtils.copyProperties(orders1,orderVO);

                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);

            }
        }
        pageResult.setRecords(list);
        // 封装数据 返回前端

        return pageResult;
    }

    /**
     * 通过order id查询 order详情
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderDetailByOrderId(Long id) {
        // 获得order数据
        Orders order = ordersMapper.getOrderById(id);

        // 获得orderDetail数据
        List<OrderDetail> orderDetails = orderDetailMapper.selectListByOrderId(order.getId());

        OrderVO orderVO = new OrderVO();

        BeanUtils.copyProperties(order,orderVO);

        orderVO.setOrderDetailList(orderDetails);


        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancelById(Long id) {
        // 待支付和待接单的状态下,用户可以直接取消订单
        Orders orders1 = ordersMapper.selectOrderById(id);

        if (orders1 == null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Integer status = orders1.getStatus();

        if(status == Orders.CONFIRMED || status == Orders.DELIVERY_IN_PROGRESS){
            throw new OrderBusinessException(MessageConstant.ORDER_CONTACT_BUSINESS);
        }



        // 如果已付款,但是商家未接单 则需要退款
        if (status == Orders.TO_BE_CONFIRMED){
            // 退款!!!,无法实现!!

        }

        // 如果未付款,则直接取消订单
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        ordersMapper.updateStatusCancelById(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    // 循环插入购物车数据, 加一个事务保安全
    @Transactional
    public void repetitionOrder(Long id) {
        /**
         * 再来一单逻辑:
         *      - 通过id查询出来该订单,查询出来之后,再重新放回购物车
         */
        Orders orders = ordersMapper.selectOrderById(id);

        List<OrderDetail> orderDetails = orderDetailMapper.selectListByOrderId(orders.getId());


        for (OrderDetail od: orderDetails){

            ShoppingCart shoppingCar = new ShoppingCart();

            BeanUtils.copyProperties(od,shoppingCar);

            shoppingCar.setCreateTime(LocalDateTime.now());

            shoppingCar.setUserId(BaseContext.getCurrentId());

            shoppingCartMapper.insert(shoppingCar);
        }
    }


    /**
     * 查询所有订单,分页查询,按条件查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 开启分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());


        // 动态查询出来 订单信息
        List<Orders> ordersList = ordersMapper.selectAllDynamic(ordersPageQueryDTO);

        if (ordersList == null || ordersList.size() == 0){
            //
        }

        // 通过order的信息查询出来地址详细信息
        for(Orders od : ordersList){
            AddressBook byId = addressBookMapper.getById(od.getAddressBookId());
            od.setAddress(byId.getDetail());
        }
        List<OrderVO> orderVOList = getOrderVOList(ordersList);

        PageInfo pageInfo = new PageInfo(ordersList);

        PageResult pageResult = new PageResult();

        pageResult.setTotal(pageInfo.getTotal());

        pageResult.setRecords(orderVOList);

        return pageResult;
    }

    private List<OrderVO> getOrderVOList(List<Orders> ordersList) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }
    /**
     * 根据订单信息获得订单字符串信息
     * @return
     */
    private String getOrderDishesStr(Orders orders){

        List<OrderDetail> orderDetailList = orderDetailMapper.selectListByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     * 查询状态,并统计
     * @return
     */
    @Override
    public OrderStatisticsVO getStatistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        // 1. 第一种实现方式:通过查询所有的orders,然后循环订单,将对应的状态进行+1
        /*List<Orders> orders = ordersMapper.selectAll();
        Integer tobeConfirmed = 0;
        Integer confirmed = 0;
        Integer deliveryInProgress = 0;

        for (Orders od : orders){
            Integer status = od.getStatus();
            if (status == Orders.TO_BE_CONFIRMED){
                tobeConfirmed += 1;
                orderStatisticsVO.setToBeConfirmed(tobeConfirmed);
            }

            if (status == Orders.CONFIRMED){
                confirmed += 1;
                orderStatisticsVO.setConfirmed(confirmed);
            }

            if (status == Orders.DELIVERY_IN_PROGRESS){
                deliveryInProgress += 1;
                orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
            }
        }*/
        // 2. 第二种实现方式:通过sql语句的count方法查询


        Integer beBeConfirmed = ordersMapper.selectCountByStatus(Orders.TO_BE_CONFIRMED);

        Integer confirmed = ordersMapper.selectCountByStatus(Orders.CONFIRMED);

        Integer deliveryInProgress = ordersMapper.selectCountByStatus(Orders.DELIVERY_IN_PROGRESS);

        orderStatisticsVO.setToBeConfirmed(beBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 修改订单状态为已经接单
     */
    @Override
    public void updateStatusById(OrdersConfirmDTO ordersConfirmDTO) {

        ordersMapper.updateStatusById(ordersConfirmDTO);
    }


    /**
     * 修改订单状态为拒绝,并添加拒绝原因
     */
    @Override
    public void updateStatusRejectById(OrdersRejectionDTO ordersRejectionDTO) {
        // 判断业务异常
        Orders orders1 = ordersMapper.selectOrderById(ordersRejectionDTO.getId());

        if (orders1 == null || orders1.getStatus() == Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        Orders orders = new Orders();
        orders.setId(ordersRejectionDTO.getId());
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        ordersMapper.updateStatusRejectById(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void updateStatusCancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());

        ordersMapper.updateStatusCancelById(orders);

    }

    /**
     * 修改状态为递送
     * @param id
     */
    @Override
    public void updateStatusDelivery(Long id) {
        Orders orders = ordersMapper.selectOrderById(id);

        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders od = new Orders();
        od.setId(orders.getId());
        // 更新订单状态,状态转为派送中
        od.setStatus(Orders.DELIVERY_IN_PROGRESS);

        ordersMapper.updateStatusDelivery(od);
    }

    /**
     * 修改订单状态为完成
     * @param id
     */
    @Override
    public void updateStatusComplete(Long id) {
        Orders orders = ordersMapper.selectOrderById(id);

        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders od = new Orders();
        od.setId(orders.getId());
        // 更新订单状态,状态为已完成
        od.setStatus(Orders.COMPLETED);
        od.setDeliveryTime(LocalDateTime.now());

        ordersMapper.updateStatusDelivery(od);
    }

    /**
     * 通过订单号 直接将订单状态改成 已付款
     */
    @Override
    public void updateStatusByNumber(OrdersPaymentDTO ordersPaymentDTO) {
        ordersMapper.updateStatusByNumberToBeConfirmed(ordersPaymentDTO);
    }

    /**
     * 催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = ordersMapper.selectOrderById(id);
        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",orders.getId());
        map.put("content","订单号:" + orders.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 统计营业额
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 拼接日期
        LocalDate beginDate = begin;
        List<LocalDate> localDates = new ArrayList<>();

        localDates.add(beginDate);
        // 计算开始时间到结束时间中间的日子,并放进list
        while (!beginDate.equals(end)){

            beginDate = beginDate.plusDays(1);
            localDates.add(beginDate);
        }

        List<Double> sumList = new ArrayList<>();
        // 根据日期,查询出来当天的营业额
        for (LocalDate localDate : localDates){
            // 生成查询当天的日期信息, 2023-02-11:00:00
            LocalDateTime localDateTimeBegin = LocalDateTime.of(localDate,LocalTime.MIN);
            // 生成当前的结束时刻   2023-02-11 23:59:59
            LocalDateTime localDateTimeEnd = LocalDateTime.of(localDate,LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin",localDateTimeBegin);
            map.put("end",localDateTimeEnd);
            map.put("status",Orders.COMPLETED);

            Double sum = ordersMapper.sumByMap(map);

            sum = sum == null?0.0:sum;
            sumList.add(sum);

        }


        TurnoverReportVO  turnoverReportVO = new TurnoverReportVO();

        //将列表转化为字符串,拼接使用 ,
        turnoverReportVO.setDateList(StringUtils.join(localDates,","));

        turnoverReportVO.setTurnoverList(StringUtils.join(sumList,","));

        return turnoverReportVO;
    }
}
