package com.lxy.gmt_mono.controller.admin;

import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.common.Result;
import com.lxy.gmt_mono.dto.TicketCreateAndUpdateRequest;
import com.lxy.gmt_mono.entity.Ticket;
import com.lxy.gmt_mono.service.SecKillService;
import com.lxy.gmt_mono.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/v1/tickets")
@Tag(name = "管理员端-票务接口", description = "票务管理")
public class AdminTicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private SecKillService secKillService;

    @PostMapping
    @Operation(summary = "创建新票务信息", description = "添加票务信息")
    public Result<Long> createTicket(@RequestBody TicketCreateAndUpdateRequest request) {
        // 1. 将请求参数转为实体对象
        Ticket ticket = new Ticket();
        BeanUtils.copyProperties(request, ticket); // 属性拷贝
        Long ticketId = ticketService.createTicket(ticket);
        return Result.success(ticketId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询指定票务的详细信息", description = "查询指定票务的详细信息")
    public Result<Ticket> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            return Result.error(ResponseCode.NOT_FOUND);
        }
        return Result.success(ticket);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新票务信息", description = "更新指定票务的详细信息")
    public Result<Void> updateTicket(@PathVariable Long id, @RequestBody TicketCreateAndUpdateRequest request) {
        Ticket ticket = new Ticket();
        BeanUtils.copyProperties(request, ticket);
        ticket.setId(id);
        ticketService.updateTicket(ticket);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除票务信息", description = "删除指定票务的详细信息")
    public Result<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return Result.success();
    }

    // todo 之后可能会改为秒杀前自动预热
    @PostMapping("/warmup/{ticketId}")
    @Operation(summary = "预热票务库存", description = "预热指定票务的库存")
    public Result<Void> warmupStock(@PathVariable Long ticketId) {
        secKillService.warmupStock(ticketId);
        return Result.success();
    }

    @PostMapping("/syncStock/{ticketId}")
    @Operation(summary = "同步票务库存到数据库", description = "同步指定票务的库存到数据库")
    public Result<Void> syncStockToDB(@PathVariable Long ticketId) {
        secKillService.syncStockToDB(ticketId);
        return Result.success();
    }

}
