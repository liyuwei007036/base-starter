package live.lumia.config.properties;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * 数据库类型
 *
 * @author liyuwei
 * @date 2021/6/7 23:18
 **/
@ConfigurationProperties(prefix = "sys.db")
@Data
public class DbTypeProperties implements InitializingBean {

    private DbType type;

    @Override
    public void afterPropertiesSet() {
        if (Objects.isNull(type)) {
            type = DbType.MYSQL;
        }
    }
}
