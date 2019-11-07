package com.lc.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author l5990
 */
@Configuration
public class RabbitConfig {

    // 发送短信 企业微信通知 推送交换机
    public static final String DIRECT_MSG_EXCHANGE = "direct.msg";
    public static final String QUEUE_SHORTMSG = "msg.short";
    public static final String QUEUE_WXMSG = "msg.wx";
    public static final String QUEUE_PUSHMSG = "msg.push";

    // 订单完成通知交换机
    public static final String TOPIC_ORDER_EXCHANGE = "topic.order";
    public static final String QUEUE_SALE = "order.sale";
    public static final String QUEUE_LOAN = "order.loan";


    /**
     * 发送短信队列
     *
     * @return
     */
    @Bean
    public Queue queueShortMsg() {
        return new Queue(QUEUE_SHORTMSG, true);
    }

    /**
     * 发送微信通知队列
     *
     * @return
     */
    @Bean
    public Queue queueWXMsg() {
        return new Queue(QUEUE_WXMSG, true);
    }

    /**
     * APP 消息推送
     *
     * @return
     */
    @Bean
    public Queue queuePush() {
        return new Queue(QUEUE_PUSHMSG, true);
    }

    @Bean
    public Queue queueLoan() {
        return new Queue(QUEUE_LOAN, true);
    }

    @Bean
    public Queue queueSale() {
        return new Queue(QUEUE_SALE, true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_MSG_EXCHANGE);
    }

    @Bean
    public TopicExchange topicOrderExchange() {
        return new TopicExchange(TOPIC_ORDER_EXCHANGE);
    }

    @Bean
    public Binding bindingDirectExchange2Short() {
        return BindingBuilder.bind(queueShortMsg()).to(directExchange()).with(QUEUE_SHORTMSG);
    }

    @Bean
    public Binding bindingDirectExchange2WX() {
        return BindingBuilder.bind(queueWXMsg()).to(directExchange()).with(QUEUE_WXMSG);
    }

    @Bean
    public Binding bindingDirectExchange2Push() {
        return BindingBuilder.bind(queueWXMsg()).to(directExchange()).with(QUEUE_PUSHMSG);
    }

    @Bean
    public Binding bindingTopicOrderExchange2Sale() {
        return BindingBuilder.bind(queueSale()).to(topicOrderExchange()).with(QUEUE_SALE);
    }

    @Bean
    public Binding bindingTopicOrderExchange2Loan() {
        return BindingBuilder.bind(queueLoan()).to(topicOrderExchange()).with(QUEUE_LOAN);
    }


}
