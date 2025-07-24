package com.lxy.gmt_mono.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // --------------- 订单服务创建订单的队列和交换机 ------------------------
    // 交换机的名字
    public static final String ORDER_EXCHANGE = "order.exchange";
    // 队列的名字
    public static final String ORDER_QUEUE = "order.queue";
    // 路由的key
    public static final String ORDER_ROUTING_KEY = "order.create";

    // --------------- 订单超时自动关单的队列和交换机 ------------------------
    // 死信交换机
    public static final String ORDER_DLX_EXCHANGE = "order.dlx.exchange";
    // 真正处理关单的队列
    public static final String ORDER_RELEASE_QUEUE = "order.release.queue";
    // 死信路由的key
    public static final String ORDER_RELEASE_ROUTING_KEY = "order.release";

    // 延迟队列
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    // 延迟队列和创建订单的队列进行绑定
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    /**
     * 声明一个主题类型的交换机，非常灵活，可以根据路由键进行复杂的匹配
     * @return TopicExchange
     */
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    /**
     * 创建一个持久化队列
     * durable=true,表示持久化，即使RabbitMQ重启，队列也不会丢失
     * @return Queue
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    /**
     * 绑定队列和交换机，并指定路由的key
     * 即所有发送到orderExchange的消息，且路由键为order.create的，都会被发送到orderQueue队列中
     * @param orderQueue 队列
     * @param orderExchange 交换机
     * @return Binding
     */
    @Bean
    public Binding orderBinding(Queue orderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    /**
     * 创建死信交换机
     * @return Queue
     */
    @Bean
    public TopicExchange orderDlxExchange() {
        return new TopicExchange(ORDER_DLX_EXCHANGE);
    }

    /**
     * 创建处理死信，进行关单的队列
     * @return Queue
     */
    @Bean
    public Queue orderReleaseQueue() {
        return new Queue(ORDER_RELEASE_QUEUE, true);
    }

    /**
     * 绑定死信队列和死信交换机
     * @param orderReleaseQueue 队列
     * @param orderDlxExchange 死信交换机
     * @return Binding
     */
    @Bean
    public Binding orderReleaseBinding(Queue orderReleaseQueue, TopicExchange orderDlxExchange) {
        return BindingBuilder.bind(orderReleaseQueue).to(orderDlxExchange).with(ORDER_RELEASE_ROUTING_KEY);
    }

    /**
     * 创建延迟队列，带有TTL和死信路由的队列
     * @return Queue
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置队列的TTL
        args.put("x-message-ttl", 600000);
        // 设置队列的死信交换机
        args.put("x-dead-letter-exchange", ORDER_DLX_EXCHANGE);
        // 设置队列的死信路由的key，由于死信交换机处理死信的队列和接收死信的队列功能都比较专一，所以这里使用同一个路由的key
        args.put("x-dead-letter-routing-key", ORDER_RELEASE_ROUTING_KEY);
        return QueueBuilder.durable(ORDER_DELAY_QUEUE).withArguments(args).build();
    }

    /**
     * 绑定延迟队列和创建订单的队列
     * @param orderDelayQueue 队列
     * @param orderExchange 交换机
     * @return Binding
     */
    @Bean
    public Binding orderDelayBinding(Queue orderDelayQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderDelayQueue).to(orderExchange).with(ORDER_DELAY_ROUTING_KEY);
    }
}
