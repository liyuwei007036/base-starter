package live.lumia.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;

import java.util.Objects;

/**
 * 模型转换
 *
 * @author lc
 * @date 2019-12-15
 */
public class ModelMapperUtils {


    private static ModelMapper getMapper(MatchingStrategy strategy) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(strategy);
        return modelMapper;
    }


    public static <T, E> T loose(E source, Class<T> type) {
        if (Objects.isNull(source)) {
            return null;
        }
        return getMapper(MatchingStrategies.LOOSE).map(source, type);
    }

    public static <T, E> T standard(E source, Class<T> type) {
        if (Objects.isNull(source)) {
            return null;
        }
        return getMapper(MatchingStrategies.STANDARD).map(source, type);
    }


    public static <T, E> T strict(E source, Class<T> type) {
        if (Objects.isNull(source)) {
            return null;
        }
        return getMapper(MatchingStrategies.STRICT).map(source, type);
    }

}
