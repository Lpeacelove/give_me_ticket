package com.lxy.gmt_mono.controller.user;

import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.common.Result;
import com.lxy.gmt_mono.entity.Ticket;
import com.lxy.gmt_mono.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "用户端-票务接口", description = "票务查询")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping
    @Operation(summary = "查询所有票务信息", description = "查询所有票务信息")
    public Result<List<Ticket>> listTicket() {
        return Result.success(ticketService.listTicket());
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
}
