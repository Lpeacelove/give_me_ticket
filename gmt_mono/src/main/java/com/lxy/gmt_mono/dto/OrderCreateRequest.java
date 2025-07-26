package com.lxy.gmt_mono.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "订单创建请求")
public class OrderCreateRequest {
    @Schema(description = "票务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long ticketId;
    @Schema(description = "购买数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer quantity = 1;
    @Schema(description = "订单防重令牌", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
}
