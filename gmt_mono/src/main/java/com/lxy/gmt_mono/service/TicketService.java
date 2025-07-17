package com.lxy.gmt_mono.service;

import com.lxy.gmt_mono.entity.Ticket;

import java.util.List;

public interface TicketService {

    /**
     * 管理端-创建节目
     *
     * @param ticket 节目对象
     * @return 节目id
     */
    Long createTicket(Ticket ticket);

    /**
     * 用户端-查询所有节目
     * @return 节目列表
     */
    List<Ticket> listTicket();

    /**
     * 用户端-查询节目详情
     * @param id 节目id
     * @return 节目对象
     */
    Ticket getTicketById(Long id);

    /**
     * 管理端-更新节目信息
     * @param ticket 节目对象
     */
    void updateTicket(Ticket ticket);

    /**
     * 管理端-删除节目
     * @param id 节目id
     */
    void deleteTicket(Long id);

    /**
     * 扣减库存
     * @param ticketId 节目id
     * @param quantity 扣减数量
     * @return 是否成功
     */
    boolean deductStock(Long ticketId, Integer quantity);
}
