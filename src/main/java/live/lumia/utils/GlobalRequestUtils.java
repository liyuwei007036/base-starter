package live.lumia.utils;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;


/**
 * @author liyuwei
 * @date 2021/6/17 09:23
 **/
public class GlobalRequestUtils {

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        return requestAttributes.getRequest();
    }

    public static HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        return requestAttributes.getResponse();
    }

    public static <T> T getData(String key, Class<T> clazz) {
        Object data = getRequest().getAttribute(key);
        if (Objects.isNull(data)){
            return null;
        }
        return ModelMapperUtils.strict(data, clazz);
    }

    public static <T> void setData(String key, T data) {
        getRequest().setAttribute(key, data);
    }


    public static String getUrl() {
        HttpServletRequest request = getRequest();
        String reqStr = request.getRequestURL().toString();
        String queryStr = request.getQueryString();
        if (StringUtils.hasText(queryStr)) {
            reqStr = reqStr + "?" + queryStr;
        }
        return reqStr;
    }
}
