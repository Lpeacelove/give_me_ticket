package com.lxy.gmt_mono.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.common.Result;
import com.lxy.gmt_mono.common.SecurityUtils;
import com.lxy.gmt_mono.dto.OrderCreateRequest;
import com.lxy.gmt_mono.dto.OrderListResponse;
import com.lxy.gmt_mono.entity.Order;
import com.lxy.gmt_mono.service.OrderService;
import com.lxy.gmt_mono.service.SecKillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "用户端-订单接口")
public class OrderController {

    @Autowired
    private SecKillService secKillService;
    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "创建订单", description = "创建订单")
    public Result<String> createOrder(@RequestBody OrderCreateRequest request) {
        // 获取当前用户id
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error(ResponseCode.FORBIDDEN);
        }
        String orderNumber = secKillService.executeSecKill(userId, request);
        return Result.success(orderNumber);
    }

    /**
     * 获取订单列表
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 订单列表
     */
    @GetMapping
    @Operation(summary = "获取订单列表", description = "获取订单列表")
    public Result<Page<OrderListResponse>> listOrdersByPage(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Long pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Long pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.listOrdersByPage(pageNum, pageSize, userId));
    }

    /**
     * 获取订单详情
     * @param orderId 订单号
     * @return 订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    public Result<Order> getOrderDetailById(
            @Parameter(description = "订单号") @PathVariable String orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(orderService.getOrderDetailById(orderId, userId));
    }

    /**
     * 支付订单
     * @param orderId 订单号
     * @return 无
     */
    @PutMapping("/{orderId}/pay")
    @Operation(summary = "支付订单")
    public Result<Void> payOrder(
            @Parameter(description = "订单ID") @PathVariable String orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderService.payOrder(orderId, userId);
        return Result.success();
    }
}
