package live.lumia.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * json web token
 * @author liyuwei
 */
@Slf4j
public class Md5SignUtils {

    public static Boolean validSign(String md5, Map<Object, Object> parameters) {
        Map<Object, Object> data = parameters.entrySet().stream()
                .filter(x -> !StringUtils.isEmpty(ObjectUtil.getString(x.getValue())) && !"sign".equals(x.getKey()) && !"timestamp".equals(x.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        String signData = ObjectUtil.getString(parameters.get("sign"));
        long timestamp = ObjectUtil.getLong(parameters.get("timestamp"));
        long s = System.currentTimeMillis() / 1000 + 3;
        if (s - timestamp > 60 * 1000 || timestamp <= 0 || s < timestamp) {
            log.warn("签名验证失败, 时间错误");
            return false;
        }
        String sbkey = data.keySet()
                .stream()
                .sorted()
                .map(x -> String.format("%s=%s", x, data.get(x)))
                .reduce((x, y) -> String.format("%s&%s", x, y))
                .orElse("") + md5;
        String sign = DigestUtils.md5DigestAsHex(sbkey.getBytes()).toUpperCase();
        if (signData.equals(sign)) {
            log.debug("签名验证,成功");
            return true;
        } else {
            log.warn(signData);
            log.warn("-------------------------------------");
            log.warn(sign);
            log.warn("签名验证失败, Sign 值不匹配");
            return false;
        }
    }
}