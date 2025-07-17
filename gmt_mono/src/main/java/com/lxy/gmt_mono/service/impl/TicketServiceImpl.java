package com.lxy.gmt_mono.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.entity.Ticket;
import com.lxy.gmt_mono.mapper.TicketMapper;
import com.lxy.gmt_mono.service.TicketService;
import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 定义一个缓存的key
    private static final String TICKET_LIST_CACHE_KEY = "ticket:list";
    private static final long CACHE_TTL = 10;
    private static final String TICKET_DETAIL_CACHE_KEY_PREFIX = "ticket:detail:";
    private static final String CACHE_NULL_VALUE = "{}";
    // 定义空值的缓存过期时间（比如5分钟），防止恶意攻击
    private static final long CACHE_NULL_TTL = 2;


    /**
     * 创建节目
     *
     * @param ticket 节目信息
     * @return 节目id
     */
    @Override
    public Long createTicket(Ticket ticket) {
        // 1. 验证节目信息
        Assert.notNull(ticket.getTitle(), "节目标题不能为空");
        Assert.notNull(ticket.getActor(), "主演不能为空");
        Assert.notNull(ticket.getPlace(), "演出地点不能为空");
        Assert.notNull(ticket.getPrice(), "价格不能为空");
        Assert.notNull(ticket.getShowTime(), "演出时间不能为空");
        Assert.isTrue(ticket.getPrice() > 0, "价格必须大于0");
        Assert.isTrue(ticket.getShowTime().isAfter(LocalDateTime.now()), "演出时间必须大于当前时间");

        // 2. 创建实体对象，准备入库
        ticket.setCreateTime(LocalDateTime.now());
        ticket.setUpdateTime(LocalDateTime.now());
        ticketMapper.insert(ticket);

        // 3. 清除缓存
        log.info("清除缓存...");
        // todo 需要优化更新缓存的逻辑
        stringRedisTemplate.delete(TICKET_LIST_CACHE_KEY);
        return ticket.getId();
    }

    /**
     * 列出节目
     * @return 节目列表
     */
    @Override
    public List<Ticket> listTicket() {
        // 先直接查询出所有数据，后续再加筛选条件

        // 1. 尝试从redis中获取数据
        log.info("尝试从redis中获取数据...");
        String cacheTicketsJson = stringRedisTemplate.opsForValue().get(TICKET_LIST_CACHE_KEY);

        // 2. 判断缓存是否击中，若击中，则直接返回缓存数据
        if (StringUtils.hasText(cacheTicketsJson)) {
            log.info("缓存击中，直接返回缓存数据...");
            try {
                return objectMapper.readValue(cacheTicketsJson, new TypeReference<List<Ticket>>(){});
            } catch (JsonProcessingException e) {
                log.info("缓存数据转换失败..."); // 缓存数据转换失败，继续进行数据库查询
            }
        }

        // 3. 缓存未击中，则从数据库中查询数据，并写入缓存
        log.info("缓存未击中，从数据库中查询数据...");
        List<Ticket> tickets = ticketMapper.selectList(null);
        try {
            String ticketsJson = objectMapper.writeValueAsString(tickets);
            long randomTimeout = CACHE_TTL + new Random().nextInt(5);
            stringRedisTemplate.opsForValue().set(TICKET_LIST_CACHE_KEY, ticketsJson, randomTimeout, TimeUnit.MINUTES);
            log.info("缓存已写入...");
        } catch (JsonProcessingException e) {
            log.error("序列化票务列表并写入缓存失败", e);
        }

        // 4. 返回查询结果
        return tickets;
    }

    /**
     * 根据id查询票务信息
     *
     * @param id 票务id
     * @return 票务信息
     */
    @Override
    public Ticket getTicketById(Long id) {
        // 1. 创建缓存key
        String cacheKey = TICKET_DETAIL_CACHE_KEY_PREFIX + id;

        // 2. 尝试查询缓存
        String cachedTicketJson = stringRedisTemplate.opsForValue().get(cacheKey);

        // 3. 判断缓存是否命中
        if (StringUtils.hasText(cachedTicketJson)) {
            log.info("票务详情缓存命中，票务id: {}", id);
            // 3.1 判断是否为设置的空值
            if (cachedTicketJson.equals(CACHE_NULL_VALUE)) {
                log.info("缓存命中，但是为空值，ticketId: {}", id);
                return null;
            }
            // 3.2 缓存命中，返回缓存数据
            try {
                return objectMapper.readValue(cachedTicketJson, Ticket.class);
            } catch (JsonProcessingException e) {
                log.error("缓存数据转换失败", e);
                // 当作没有缓存处理，去数据库中读取数据
            }
        }

        // 4. 缓存未命中，去数据库中查询数据
        log.info("缓存未命中，去数据库中查询数据");
        Ticket ticketFromDb = ticketMapper.selectById(id);

        // 5. 整理提取到的数据
        // 5.1 如果不为空, 缓存数据
        try {
            if (ticketFromDb != null) {
                cachedTicketJson = objectMapper.writeValueAsString(ticketFromDb);
                long randomTimeout = CACHE_TTL + new Random().nextInt(5);
                stringRedisTemplate.opsForValue().set(cacheKey, cachedTicketJson, randomTimeout, TimeUnit.MINUTES);
                log.info("票务详情数据缓存成功");
            } else {
                // 5.2 如果为空, 缓存空值, 避免缓存穿透，并设置一个较短的过期时间，防止恶意攻击
                stringRedisTemplate.opsForValue().set(cacheKey, CACHE_NULL_VALUE, CACHE_NULL_TTL, TimeUnit.MINUTES);
                log.info("票务详情在数据库中不存在，缓存空值");

            }
        } catch (JsonProcessingException e) {
            log.error("JSON序列化错误：{}", e.getMessage());
        }

        return ticketFromDb;
    }


    /**
     * 更新票务信息
     *
     * @param ticket 票务信息
     */
    @Override
    public void updateTicket(Ticket ticket) {
        // 1. 判断该票务是否存在
        Ticket existingTicket = ticketMapper.selectById(ticket.getId());
        Assert.notNull(existingTicket, "票务不存在");
        // 2. 更新票务信息
        ticketMapper.updateById(ticket);

        // 3. 删除缓存
        stringRedisTemplate.delete(TICKET_DETAIL_CACHE_KEY_PREFIX + ticket.getId());
        stringRedisTemplate.delete(TICKET_LIST_CACHE_KEY);
        log.info("更新票务信息成功");
    }


    /**
     * 删除票务信息
     *
     * @param id 票务id
     */
    @Override
    public void deleteTicket(Long id) {
        // 1. 判断票务是否存在
        Ticket existingTicket = ticketMapper.selectById(id);
        Assert.notNull(existingTicket, "票务不存在");
        // 2. 删除票务
        ticketMapper.deleteById(id);
        // 3. 删除缓存
        stringRedisTemplate.delete(TICKET_DETAIL_CACHE_KEY_PREFIX + id);
        stringRedisTemplate.delete(TICKET_LIST_CACHE_KEY);
        log.info("删除票务信息成功");
    }

    /**
     * 扣减库存
     *
     * @param ticketId 票务id
     * @param quantity 扣减数量
     * @return 是否扣减成功
     */
    @Override
    public boolean deductStock(Long ticketId, Integer quantity) {
        // 1. 查看当前id对应的票务信息
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null || ticket.getStock() < quantity) {
            return false;
        }

        // 2. 扣减库存
        Long remainingStock = ticket.getStock() - quantity;
        // 使用乐观锁进行更新
        LambdaUpdateWrapper<Ticket> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(Ticket::getId, ticketId)
                .eq(Ticket::getStock, ticket.getStock())  // 乐观锁，确保在我们操作的时候，没有人修改库存
                .set(Ticket::getStock, remainingStock);
        int updateCount = ticketMapper.update(null, updateWrapper);
        // todo 暂时直接删除缓存，后续会根据事务是否完成判断缓存是否要删除
        // if (updatedRows > 0) {
        //        // 更新成功了，但是不立刻删除缓存
        //        // 而是把“删除缓存”这个任务，注册到当前事务的“成功提交后”回调中
        //        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        //            @Override
        //            public void afterCommit() {
        //                // 这个方法会在整个外部事务（比如createOrder的事务）成功提交之后才被调用
        //                String detailCacheKey = TICKET_DETAIL_CACHE_KEY_PREFIX + ticketId;
        //                stringRedisTemplate.delete(detailCacheKey);
        //                stringRedisTemplate.delete(TICKET_LIST_CACHE_KEY);
        //                log.info("事务已提交，票务[ID:{}]相关缓存已删除", ticketId);
        //            }
        //        });
        //    }
        //
        if (updateCount > 0) {
            stringRedisTemplate.delete(TICKET_DETAIL_CACHE_KEY_PREFIX + ticketId);
            stringRedisTemplate.delete(TICKET_LIST_CACHE_KEY);
            log.info("扣减库存成功");
        }
        return updateCount > 0;
    }


}
