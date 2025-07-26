package com.lxy.gmt_mono.service;

import com.lxy.gmt_mono.dto.OrderCreateRequest;

public interface SecKillService {

    /**
     * 用户端-执行秒杀操作携带防重令牌
     *
     * @param userId  用户id
     * @param request 订单创建请求
     * @return 秒杀结果
     */
    String executeSecKillWithToken(Long userId, OrderCreateRequest request);

    /**
     * 用户端-执行秒杀操作
     *
     * @param userId  用户id
     * @param request 订单创建请求
     * @return 秒杀结果
     */
    String executeSecKill(Long userId, OrderCreateRequest request);

    /**
     * 管理端-预热库存
     *
     * @param ticketId 票务ID
     */
    void warmupStock(Long ticketId);

    /**
     * 管理端-同步库存到数据库
     *
     * @param ticketId 票务ID
     */
    void syncStockToDB(Long ticketId);
}
