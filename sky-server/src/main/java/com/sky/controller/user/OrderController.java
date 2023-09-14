package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderDetailService;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/12 15:40
 */
@Slf4j
@Api(tags = "订单相关功能")
@RestController("userOrderController")
@RequestMapping("/user/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        OrderSubmitVO submit = orderService.submit(ordersSubmitDTO);

        return Result.success(submit);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);


        // 直接修订单的状态为已支付
        orderService.updateStatusByNumber(ordersPaymentDTO);

        //直接调用支付成功的回调方法,假设已经支付成功了
        // orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        orderService.sendWsComeOrder(ordersPaymentDTO.getOrderNumber());

        return Result.success(orderPaymentVO);
    }



    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> getHistoryOrders(Integer page,Integer pageSize, Integer status){
        PageResult historyOrdersPage = orderService.getHistoryOrdersPage(page, pageSize, status);


        return Result.success(historyOrdersPage);
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getOrderDetailByOrderId(@PathVariable("id") Long id){
        OrderVO orderVO = orderService.getOrderDetailByOrderId(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancelById(@PathVariable("id") Long id){
        orderService.cancelById(id);

        return Result.success();
    }


    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetitionOrder(@PathVariable("id") Long id){
        orderService.repetitionOrder(id);
        return Result.success();
    }

    @GetMapping("reminder/{id}")
    @ApiOperation("催单")
    public Result reminder(@PathVariable("id") Long id){
        orderService.reminder(id);
        return Result.success();
    }

}
