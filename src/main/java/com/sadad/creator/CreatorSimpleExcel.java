package com.sadad.creator;

import com.sadad.enums.ExportType;
import org.apache.commons.lang3.StringUtils;
import com.sadad.model.ImmutableTriple;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class CreatorSimpleExcel {

    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    Object createExportFromList(List<List<String>> requestList, ExportType exportType, String name, Map<String, Integer> mergeIndex) {
        if (exportType.equals(ExportType.NONE)) {
            // return List
            return requestList;
        } else if (exportType.equals(ExportType.EXCEL)) {
            // return InputStream
            return createExcel(requestList, name, mergeIndex);
        } else if (exportType.equals(ExportType.CSV)) {
            // return InputStream
            return createCSV(requestList);
        }

        return null;
    }

    private InputStream createExcel(List<List<String>> requestList, String sheetName, Map<String, Integer> mergeIndex) {
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

            mergeColumn(sheet, mergeIndex);

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

    private void mergeColumn(XSSFSheet sheet, Map<String, Integer> mergeIndex) {
        if (mergeIndex != null && !mergeIndex.isEmpty()) {

            // Index, From, To
            List<ImmutableTriple<Integer, Integer, Integer>> immutableTripleList = new ArrayList<>();
            mergeIndex.forEach((key, value) -> {
                immutableTripleList.add(new ImmutableTriple<>(value, 0, 0));
            });

            int lengthRowSheet = sheet.getLastRowNum();

            immutableTripleList.forEach(imm -> {
                for (int i = 1; i < lengthRowSheet; i++) {
                    Row r = sheet.getRow(i);
                    Cell cell = r.getCell(imm.getLeft());

                    if (cell != null && imm.getMiddle() >= imm.getRight() && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty()) {
                        imm.setRight(i);

                        if(imm.getRight() - imm.getMiddle() >= 2) {
                            CellRangeAddress cellAddresses = new CellRangeAddress(imm.getMiddle(), imm.getRight() - 1, imm.getLeft(), imm.getLeft());
                            sheet.addMergedRegion(cellAddresses);
                        }

                        // Find Last Index That Such As Strings
                        // Then Must
                        i--;
                        continue;
                    }

                    if (cell != null && cell.getStringCellValue() != null && !cell.getStringCellValue().isEmpty())
                        imm.setMiddle(i);
                }
            });
        }
    }
}
