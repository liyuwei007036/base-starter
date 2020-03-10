package com.lc.core.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author l5990
 */
@ConditionalOnClass(RabbitAdmin.class)
@Configuration
public class RabbitConfig {

    /**
     * 请求交换机
     */
    public static final String REQUEST_EXCHANGE = "request";
    /**
     * 请求队列
     */
    public static final String REQUEST_QUEUE = "request.log";


    @Bean
    public Queue requestQueue() {
        return new Queue(REQUEST_QUEUE, true);
    }


    @Bean
    public DirectExchange requestExchange() {
        return new DirectExchange(REQUEST_EXCHANGE);
    }


    @Bean
    public Binding bindingRequestExchange() {
        return BindingBuilder.bind(requestQueue()).to(requestExchange()).with(REQUEST_QUEUE);
    }


}
