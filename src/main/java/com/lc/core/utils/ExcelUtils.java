package com.lc.core.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.lc.core.dto.ExcelWriteData;
import com.lc.core.service.ExcelReadListener;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author liyuwei
 */
public class ExcelUtils {

    /**
     * 简单读
     *
     * @param excelFile excel文件
     * @param batchNum  批量处理数量
     * @param func      配置
     * @param head      头部解析
     * @param headerNum 头的数量
     * @param <T>       解析类型
     */
    public static <T> void readSimple(File excelFile, Integer batchNum, Consumer<List<T>> func, Class<T> head, Integer headerNum, Integer... sheets) {
        ExcelReadListener<T> service = new ExcelReadListener<>(batchNum, func);
        ExcelReader excelReader = EasyExcel.read(excelFile, head, service).headRowNumber(headerNum).build();
        for (Integer sheet : sheets) {
            ReadSheet readSheet = EasyExcel.readSheet(sheet).build();
            excelReader.read(readSheet);
        }
        excelReader.finish();
    }

    /**
     * 写一个sheet
     *
     * @param excelFile 被写入文件
     * @param head      标题
     * @param data      写入数据
     * @param sheetName sheet名称
     * @param <T>       标题解析对象
     * @param <E>       写入数据类型
     */
    public static <T, E> void writeSimple(File excelFile, Class<T> head, List<E> data, String sheetName) {
        EasyExcel.write(excelFile, head).sheet(sheetName).doWrite(data);
    }

    /**
     * 写多个sheet
     *
     * @param excelFile 被写入文件
     * @param data      写入数据集合
     * @param <T>       标题解析对象
     * @param <E>       写入数据类型
     */
    public static <T, E> void writeMultipleSheetSimple(File excelFile, List<ExcelWriteData<T, E>> data) {
        ExcelWriter excelWriter = EasyExcel.write(excelFile).build();
        data.forEach(x -> {
            WriteSheet writeSheet = EasyExcel.writerSheet(x.getSheetName()).head(x.getHeader()).build();
            excelWriter.write(x.getData(), writeSheet);
        });
        excelWriter.finish();
    }
}
