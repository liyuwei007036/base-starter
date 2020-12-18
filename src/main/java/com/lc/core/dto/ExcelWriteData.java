package com.lc.core.dto;

import lombok.Data;

import java.util.List;

/**
 * 多个Sheet写入对象
 *
 * @param <T>
 * @param <E>
 * @author liyuwei
 */
@Data
public class ExcelWriteData<T, E> {

    /**
     * 数据类型
     */
    private List<T> data;

    /**
     * 头类型
     */
    private Class<E> header;

    /**
     * 写入的sheet的名称
     */
    private String sheetName;

}
