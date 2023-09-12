package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        if (addressBook == null
        ){
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



}
