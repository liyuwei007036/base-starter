package live.lumia.utils;

import live.lumia.enums.BaseErrorEnums;
import live.lumia.error.BaseException;
import live.lumia.error.IErrorInterface;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.*;

/**
 * @author lc
 * @date 2020/3/3下午 7:57
 */
public class ValidatorUtil {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();


    public static <T> void validator(T object, Class<?>... groups) {
        if (object == null) {
            throw new BaseException(BaseErrorEnums.ERROR_ARGS);
        }

        if (groups == null) {
            groups = new Class[]{Default.class};
        }
        Set<ConstraintViolation<T>> set = VALIDATOR.validate(object, groups);
        Map<String, String> errorMap = new HashMap<>(16);
        if (!CollectionUtils.isEmpty(set)) {
            Iterator<ConstraintViolation<T>> iterator = set.iterator();
            String property;
            while (iterator.hasNext()) {
                ConstraintViolation<T> c = iterator.next();
                property = c.getPropertyPath().toString();
                if (StringUtils.isEmpty(property)) {
                    continue;
                }
                String value = errorMap.get(property);
                if (!StringUtils.isEmpty(value)) {
                    value = String.format("%s:%s", errorMap.get(property), c.getMessage());
                } else {
                    value = c.getMessage();
                }
                errorMap.put(property, value);

                if (!errorMap.isEmpty()) {
                    String err = errorMap.entrySet().stream()
                            .map(x -> String.format("%s:%s", x.getKey(), x.getValue()))
                            .reduce((x, y) -> String.format("%s;%s", x, y)).orElse("");
                    throw new BaseException(new IErrorInterface() {
                        @Override
                        public int getCode() {
                            return BaseErrorEnums.ARGS_FORMAT_ERROR.getCode();
                        }

                        @Override
                        public String getMsg() {
                            return err;
                        }
                    });
                }
            }
        }
    }
}
