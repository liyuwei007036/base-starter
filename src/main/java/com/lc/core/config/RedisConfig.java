package com.lc.core.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lc.core.config.properties.RedisConfigProperties;
import com.lc.core.service.RedisService;
import com.lc.core.utils.ObjectUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author l5990
 */
@Log4j2
@EnableConfigurationProperties(RedisConfigProperties.class)
public class RedisConfig {

    @Autowired
    private RedisConfigProperties redisConfigProperties;

    @ConditionalOnProperty(prefix = "redis", name = "model", havingValue = "standalone")
    @Bean("jedisConnectionFactory")
    public JedisConnectionFactory standaloneJedisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(redisConfigProperties.getHostName());
        redisStandaloneConfiguration.setPort(redisConfigProperties.getPort());
        //由于我们使用了动态配置库,所以此处省略
        redisStandaloneConfiguration.setPassword(RedisPassword.of(redisConfigProperties.getPassword()));

        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration.builder()
                .usePooling().poolConfig(jedisPoolConfig).and()
                .connectTimeout(Duration.ofMillis(redisConfigProperties.getConnectTimeout()))
                .readTimeout(Duration.ofSeconds(redisConfigProperties.getReadTimeout()));


        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
    }

    @ConditionalOnProperty(prefix = "redis", name = "model", havingValue = "sentinel")
    @Bean("jedisConnectionFactory")
    public JedisConnectionFactory sentinelJedisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.setMaster(redisConfigProperties.getMasterName());
        redisSentinelConfiguration.setPassword(RedisPassword.of(redisConfigProperties.getPassword()));
        List<RedisNode> nodes = redisConfigProperties.getNodes().parallelStream().map(x -> {
            String host = x.split(":")[0];
            int port = ObjectUtil.getInteger(x.split(":")[1]);
            return new RedisNode(host, port);
        }).collect(Collectors.toList());
        redisSentinelConfiguration.setSentinels(nodes);
//
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder()
                .connectTimeout(Duration.ofMillis(redisConfigProperties.getConnectTimeout()))
                .readTimeout(Duration.ofSeconds(redisConfigProperties.getReadTimeout()))
                .usePooling().poolConfig(jedisPoolConfig).build();

        return new JedisConnectionFactory(redisSentinelConfiguration, jedisClientConfiguration);
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(redisConfigProperties.getMaxIdle());
        jedisPoolConfig.setMaxTotal(redisConfigProperties.getMaxTotal());
        jedisPoolConfig.setMaxWaitMillis(redisConfigProperties.getMaxWaitMillis());
        jedisPoolConfig.setMinIdle(redisConfigProperties.getMinIdle());
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(redisConfigProperties.getTimeBetweenEvictionRunsMillis());
        jedisPoolConfig.setNumTestsPerEvictionRun(redisConfigProperties.getNumTestsPerEvictionRun());
        jedisPoolConfig.setTestOnBorrow(redisConfigProperties.getTestOnBorrow());
        jedisPoolConfig.setTestWhileIdle(redisConfigProperties.getTestWhileIdle());
        jedisPoolConfig.setTestOnReturn(redisConfigProperties.getTestIbOnReturn());
        // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        jedisPoolConfig.setBlockWhenExhausted(true);
        // 是否启用pool的jmx管理功能, 默认true
        jedisPoolConfig.setJmxEnabled(true);
        return jedisPoolConfig;
    }


    @Bean
    public RedisTemplate functionDomainRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        log.info("RedisTemplate实例化成功！");
        RedisTemplate redisTemplate = new RedisTemplate();
        initDomainRedisTemplate(redisTemplate, jedisConnectionFactory);
        return redisTemplate;
    }


    private void initDomainRedisTemplate(RedisTemplate redisTemplate, RedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        redisTemplate.setEnableDefaultSerializer(true);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.afterPropertiesSet();
    }

    @Bean(name = "redisService")
    public RedisService redisService(RedisTemplate redisTemplate) {
        log.info("redisService 注入成功！");
        RedisService<Object, Object> redisService = new RedisService<>();
        redisService.setRedisTemplate(redisTemplate);
        return redisService;
    }

}
