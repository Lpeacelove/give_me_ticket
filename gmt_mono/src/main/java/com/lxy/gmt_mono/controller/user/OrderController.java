package com.lxy.gmt_mono.controller.user;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "用户端-订单接口")
public class OrderController {

    @Autowired
    private SecKillService secKillService;
    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     *
     * @param request 订单信息
     * @return 订单号
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "创建订单")
    @SentinelResource(value = "createOrder", blockHandler = "handleCreateOrderBlock")
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
    @PutMapping(value = "/{orderId}/pay", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "支付订单", description = "返回一个可供浏览器直接渲染的支付宝支付页面HTML")
    public String payOrder(
            @Parameter(description = "订单ID") @PathVariable String orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        String payHtml = orderService.payOrder(orderId, userId);
        return payHtml;
    }

    /**
     * 创建订单限流处理方法
     * @param request 订单信息
     * @param exception 限流异常
     * @return 错误信息
     */
    public Result<String> handleCreateOrderBlock(OrderCreateRequest request, BlockException exception) {
        log.warn("创建订单接口被限流，请求信息：{}", request);
        return Result.error(ResponseCode.TOO_MANY_REQUESTS);
    }

    /**
     * 取消订单
     * @param orderId 订单号
     * @return 无
     */
    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID")
            @PathVariable String orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        orderService.cancelOrder(orderId, userId);
        return Result.success();
    }
}
