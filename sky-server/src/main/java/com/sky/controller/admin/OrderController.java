package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/13 11:39
 */
@RestController("adminOrderController")
@Api(tags = "管理端订单模块")
@Slf4j
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    @ApiOperation("订单管理接口")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("orderPageQueryDTO:{}",ordersPageQueryDTO);

        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);


        return Result.success(pageResult);
    }


    @GetMapping("/statistics")
    @ApiOperation("统计各个状态的订单数量")
    public Result getStatistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.getStatistics();

        return Result.success(orderStatisticsVO);
    }

    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getDetails(@PathVariable Long id){
        OrderVO orderDetailByOrderId = orderService.getOrderDetailByOrderId(id);

        return Result.success(orderDetailByOrderId);
    }

    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result updateStatusConfirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        ordersConfirmDTO.setStatus(Orders.CONFIRMED);

        orderService.updateStatusById(ordersConfirmDTO);

        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result updateStatusRejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){

        orderService.updateStatusRejectById(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result updateStatusCancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        orderService.updateStatusCancel(ordersCancelDTO);

        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result updateStatusDelivery(@PathVariable Long id){
        orderService.updateStatusDelivery(id);

        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result updateStatusComplete(@PathVariable Long id){
        orderService.updateStatusComplete(id);
        return Result.success();
    }
}
