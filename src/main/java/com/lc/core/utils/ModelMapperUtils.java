package com.lc.core.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

/**
 * 模型转换
 *
 * @author lc
 * @date 2019-12-15
 */
public class ModelMapperUtils {


    private ModelMapper getMapper() {
        return new ModelMapper();
    }


    public <T, E> T loose(E source, Class<T> type) {
        ModelMapper modelMapper = getMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
        return modelMapper.map(source, type);
    }

    public <T, E> T standard(E source, Class<T> type) {
        ModelMapper modelMapper = getMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STANDARD);
        return modelMapper.map(source, type);
    }


    public <T, E> T strict(E source, Class<T> type) {
        ModelMapper modelMapper = getMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(source, type);
    }

}
