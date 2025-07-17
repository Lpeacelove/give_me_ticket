package com.lxy.gmt_mono.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "订单列表响应")
public class OrderListResponse {
    @Schema(description = "订单ID")
    private String orderNumber;
    @Schema(description = "订单标题")
    private String ticketTitle;
    @Schema(description = "总金额")
    private Double orderPrice;
    @Schema(description = "下单时间")
    private LocalDateTime createOrderTime;
    @Schema(description = "取消时间")
    private LocalDateTime cancelOrderTime;
    @Schema(description = "订单状态")
    private Integer status; // 订单状态: 0: 待支付 1: 已支付 2: 已取消 3: 已完成
}
