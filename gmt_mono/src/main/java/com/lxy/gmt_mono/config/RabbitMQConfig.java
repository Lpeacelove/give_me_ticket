package com.lxy.gmt_mono.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 交换机的名字
    public static final String ORDER_EXCHANGE = "order.exchange";
    // 队列的名字
    public static final String ORDER_QUEUE = "order.queue";
    // 路由的key
    public static final String ORDER_ROUTING_KEY = "order.create";

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
}
