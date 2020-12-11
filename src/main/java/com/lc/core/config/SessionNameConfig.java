package com.lc.core.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liyuwei
 */
@ConfigurationProperties(prefix = "sys.session")
@Data
public class SessionNameConfig {

    private String name;

    private Long timeout;
}
