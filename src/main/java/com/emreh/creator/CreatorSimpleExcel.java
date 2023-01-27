package com.emreh.creator;

import com.emreh.enums.ExportType;
import com.emreh.model.ColumnModel;
import com.emreh.model.ImmutableTriple;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class CreatorSimpleExcel<T> {

    // For UTF-8 Encoding
    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    Object createExportFromList(List<List<String>> requestList, ExportType exportType, String name, ColumnModel columnModel) {
        if (exportType.equals(ExportType.NONE)) {
            // return List
            return requestList;
        } else if (exportType.equals(ExportType.EXCEL)) {
            // return InputStream
            return createExcel(requestList, name, columnModel);
        } else if (exportType.equals(ExportType.CSV)) {
            // return InputStream
            return createCSV(requestList);
        }

        return null;
    }

    private InputStream createExcel(List<List<String>> requestList, String sheetName, ColumnModel columnModel) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            //Create a blank sheet
            XSSFSheet sheet = workbook.createSheet(sheetName);
            sheet.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);
            sheet.createFreezePane(0, 1);

            DataFormat format = workbook.createDataFormat();

            Font font = workbook.createFont();
            font.setColor(IndexedColors.WHITE.getIndex());
            font.setFontHeightInPoints((short) 1);
            font.setBold(true);

            AtomicInteger rowIndex = new AtomicInteger(0);
            requestList.forEach(rowModel -> {
                Row row = sheet.createRow(rowIndex.get());

                CellStyle styleOther = workbook.createCellStyle();
                styleOther.setDataFormat(format.getFormat("0"));
                styleOther.setVerticalAlignment(VerticalAlignment.CENTER);

                if (rowIndex.get() % 2 == 0) {
                    styleOther.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                    styleOther.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                }

                AtomicInteger counter = new AtomicInteger();
                rowModel.forEach(model -> {
                    Cell cell = row.createCell(counter.get());
                    cell.setCellValue(model);
                    cell.setCellStyle(styleOther);
                    counter.getAndIncrement();
                });

                rowIndex.getAndIncrement();
            });

            for (int i = 0; i < rowIndex.get(); i++)
                sheet.autoSizeColumn(i);

            mergeColumn(sheet, columnModel);
            setFilter(sheet, columnModel);

            workbook.write(bos);
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private InputStream createCSV(List<List<String>> requestList) {

        try {
            AtomicReference<StringBuilder> csvString = new AtomicReference<>();
            requestList.forEach(sub -> {
                StringBuilder builder = new StringBuilder();

                if (csvString.get() != null)
                    builder.append(csvString.get());

                builder.append(StringUtils.join(sub, ",")).append("\n");
                csvString.set(builder);
            });

            String csv = new String(BOM) + csvString.get().toString();

            return new ByteArrayInputStream(csv.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // For POI (Excel)
    private void mergeColumn(XSSFSheet sheet, ColumnModel columnModel) {
        if (columnModel != null && columnModel.getColumnModelDetailsList() != null && !columnModel.getColumnModelDetailsList().isEmpty()) {

            // Index, From, To
            List<ImmutableTriple<Integer, Integer, Integer>> immutableTripleList = new ArrayList<>();
            columnModel.getColumnModelDetailsList().forEach(value -> {
                if (value.isMerge())
                    immutableTripleList.add(new ImmutableTriple<>(value.getIndex(), 0, 0));
            });

            // If Is Not Define Column For Merge Row
            if (immutableTripleList.isEmpty()) return;

            int lengthRowSheet = sheet.getLastRowNum();

            immutableTripleList.forEach(imm -> {
                for (int i = 1; i < lengthRowSheet; i++) {
                    Row r = sheet.getRow(i);
                    Cell cell = r.getCell(imm.getLeft());

                    if (cell != null && imm.getMiddle() >= imm.getRight() && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()) {
                        imm.setRight(i);

                        if (imm.getRight() - imm.getMiddle() >= 2 && imm.getMiddle() != 0)
                            sheet.addMergedRegion(new CellRangeAddress(imm.getMiddle(), imm.getRight() - 1, imm.getLeft(), imm.getLeft()));

                        // Find Last Index That Such As Strings
                        // Then Must
                        i--;
                        continue;
                    }

                    if (cell != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty() && i != 0)
                        imm.setMiddle(i);
                }
            });
        }
    }

    private void setFilter(XSSFSheet sheet, ColumnModel columnModel) {
        if (columnModel != null && columnModel.isFilter()) {

            int lastCellNum = sheet.getRow(0).getLastCellNum();
            sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, lastCellNum - 1));
        }
    }
}
