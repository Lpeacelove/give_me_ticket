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
@Schema(description = "节目信息")
public class TicketCreateAndUpdateRequest {
    @Schema(description = "节目名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "《唐顿庄园》")
    private String title;
    @Schema(description = "主演", requiredMode = Schema.RequiredMode.REQUIRED, example = "Lady Mary")
    private String actor;
    @Schema(description = "演出地点", requiredMode = Schema.RequiredMode.REQUIRED, example = "天津市")
    private String place;
    @Schema(description = "价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "10.0")
    private Double price;
    @Schema(description = "库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long stock;
    @Schema(description = "演出时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2022-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime showTime;
}
