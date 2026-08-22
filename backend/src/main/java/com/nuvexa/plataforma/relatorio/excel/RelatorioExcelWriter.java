package com.nuvexa.plataforma.relatorio.excel;

import com.nuvexa.plataforma.relatorio.RelatorioColuna;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;

public final class RelatorioExcelWriter {

    private RelatorioExcelWriter() {
    }

    public static <T> byte[] write(String sheetName, List<RelatorioColuna<T>> columns, List<T> rows) {
        List<RelatorioColuna<T>> sortedColumns = columns.stream()
                .sorted(Comparator.comparingInt(RelatorioColuna::getOrder))
                .toList();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            writeHeader(workbook, sheet, sortedColumns);
            writeRows(sheet, sortedColumns, rows);

            for (int i = 0; i < sortedColumns.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar planilha do relatório", e);
        }
    }

    private static <T> void writeHeader(Workbook workbook, Sheet sheet, List<RelatorioColuna<T>> columns) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i).getLabel());
            cell.setCellStyle(headerStyle);
        }
    }

    private static <T> void writeRows(Sheet sheet, List<RelatorioColuna<T>> columns, List<T> rows) {
        int rowIndex = 1;
        for (T row : rows) {
            Row dataRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < columns.size(); i++) {
                setCellValue(dataRow.createCell(i), columns.get(i).getValueExtractor().apply(row));
            }
        }
    }

    private static void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else {
            cell.setCellValue(value.toString());
        }
    }
}
