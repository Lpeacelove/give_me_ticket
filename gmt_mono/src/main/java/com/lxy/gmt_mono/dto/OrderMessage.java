package com.lxy.gmt_mono.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessage {
    private Long userId;
    private Long ticketId;
    private Integer quantity;
    private String orderNumber;  // 可以提前生成订单号
}
