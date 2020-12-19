package live.lumia.service;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellData;
import com.alibaba.excel.metadata.CellExtra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ExcelReadListener<T> extends AnalysisEventListener<T> {


    /**
     * 批量处理大小
     */
    private Integer batchNum = 0;

    private final List<T> list = new ArrayList<>();

    private Consumer<List<T>> invokeFunc;

    private ExcelReadListener() {
    }

    public ExcelReadListener(Integer batchNum, Consumer<List<T>> invokeFunc) {
        this.batchNum = Optional.ofNullable(batchNum).orElse(0);
        this.invokeFunc = invokeFunc;
    }

    @Override
    public void invokeHead(Map<Integer, CellData> headMap, AnalysisContext context) {
        super.invokeHead(headMap, context);
    }

    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        list.add(t);
        if (batchNum > 0) {
            if (list.size() >= batchNum) {
                invokeFunc.accept(list);
                list.clear();
            }
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        super.invokeHeadMap(headMap, context);
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        super.extra(extra, context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        invokeFunc.accept(list);
        list.clear();
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        super.onException(exception, context);
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }
}
