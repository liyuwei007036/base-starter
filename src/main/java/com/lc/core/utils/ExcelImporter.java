package com.lc.core.utils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * excel 导入工具
 * @author l5990
 */
@Data
@Log4j2
public class ExcelImporter {

    static final int MAX_BLANK_ROW = 2;

    /**
     * 导入数据的开始行数
     */
    private int startRow = 1;

    /**
     * 导入数据结束行
     */
    private int endRow = -1;

    /**
     * 列名对应关系
     */
    private Map<String, String> columnMap;

    private Workbook workbook = null;

    private FormulaEvaluator evaluator = null;

    private String dateFormat = "yyyy-MM-dd";

    private DecimalFormat df = new DecimalFormat("#0.###");

    public ExcelImporter(File file, Map<String, String> columnMap, int startRow, int endRow) {
        try {
            this.columnMap = columnMap;
            this.startRow = startRow;
            this.endRow = endRow;
            workbook = WorkbookFactory.create(new FileInputStream(file));
            evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("导入出错", e);
        }
    }

    public ExcelImporter(FileInputStream file, Map<String, String> columnMap, int startRow, int endRow) {
        try {
            this.columnMap = columnMap;
            this.startRow = startRow;
            this.endRow = endRow;
            workbook = WorkbookFactory.create(file);
            evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("导入出错", e);
        }
    }

    public String getColumnName(int colIndex) {
        // result
        StringBuilder strResult = new StringBuilder();
        // remainder
        int iRest = 0;
        while (colIndex != 0) {
            iRest = colIndex % 26;
            char ch = ' ';
            if (iRest == 0) {
                ch = 'Z';
            } else {
                ch = (char) (iRest - 1 + 'A');
            }
            strResult.insert(0, String.valueOf(ch));
            if (strResult.charAt(0) == 'Z') {
                colIndex = colIndex / 26 - 1;
            } else {
                colIndex /= 26;
            }
        }
        return strResult.toString();
    }

    public List<Map<String, Object>> getData() {
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            return null;
        }
        boolean blankRowBreak = false;
        int blankRowCount = 0;
        List<Map<String, Object>> returnList = new ArrayList<>();
        for (int rowIndex = startRow; (rowIndex < endRow || ((endRow == -1)) && !blankRowBreak); rowIndex++) {
            HashedMap<String, Object> returnParams = getOneRowData(sheet, rowIndex);
            if ((returnParams == null) || (returnParams.size() < 1)) {
                blankRowCount += 1;
            } else {
                blankRowCount = 0;
                returnList.add(returnParams);
            }
            blankRowBreak = (blankRowCount > MAX_BLANK_ROW);
        }
        return returnList;
    }

    public HashedMap<String, Object> getOneRowData(Sheet currentSheet, int rowIndex) {
        HashedMap<String, Object> returnParams = new HashedMap<>();
        Row row = currentSheet.getRow(rowIndex);
        if (row == null) {
            return returnParams;
        }
        for (Map.Entry<String, String> entry : columnMap.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            returnParams.put(v, getCellValueByColName(row, k));
        }
        return returnParams;
    }

    public String getCellValueByColName(Row row, String columnName) {
        int colIndex = CellReference.convertColStringToIndex(columnName);
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        if (evaluator != null) {
            cell = evaluator.evaluateInCell(cell);
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                // 判断是否为日期
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date javaDate = cell.getDateCellValue();
                    return DateUtils.dateToStr(javaDate, dateFormat);
                } else {
                    double value = cell.getNumericCellValue();
                    return df.format(value);
                }
            case BOOLEAN:
                if (cell.getBooleanCellValue()) {
                    return "T";
                }
                return "F";
            default:
                return cell.getStringCellValue();
        }
    }

}
