package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
