package live.lumia.config.message;

import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 请求日志队列
 *
 * @author l5990
 */
@Configuration
@ConditionalOnBean({RabbitTemplate.class, RedissonClient.class})
public class RabbitRequestLogConfig {

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
