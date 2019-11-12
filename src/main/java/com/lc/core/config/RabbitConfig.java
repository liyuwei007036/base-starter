package com.lc.core.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author l5990
 */
@Configuration
public class RabbitConfig {

    /**
     * 发送通知 推送交换机
     */
    public static final String DIRECT_MSG_EXCHANGE = "direct.msg";
    /**
     * 发送通知 推送交换机
     */
    public static final String QUEUE_SHORTMSG = "msg.short";


    /**
     * 发送短信队列
     *
     * @return
     */
    @Bean
    public Queue queueShortMsg() {
        return new Queue(QUEUE_SHORTMSG, true);
    }


    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_MSG_EXCHANGE);
    }


    @Bean
    public Binding bindingDirectExchange2Short() {
        return BindingBuilder.bind(queueShortMsg()).to(directExchange()).with(QUEUE_SHORTMSG);
    }


}
