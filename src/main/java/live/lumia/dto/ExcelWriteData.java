package live.lumia.dto;

import lombok.Data;

import java.util.List;

/**
 * 多个Sheet写入对象
 *
 * @param <D> 数据类型
 * @param <H> 头类型
 * @author liyuwei
 */
@Data
public class ExcelWriteData<D, H> {

    /**
     * 数据类型
     */
    private List<D> data;

    /**
     * 头类型
     */
    private Class<H> header;

    /**
     * 写入的sheet的名称
     */
    private String sheetName;

}
