package com.project.lexerjava;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Export Utility (Apache POI layer).
 * Writes tokens + errors to either a real .xlsx workbook (two sheets:
 * "Tokens" and "Errors") or a plain .csv file. No JavaFX dependency -
 * the Controller decides which method to call based on chosen extension.
 */
public class ExcelExportUtil {

    public static void exportToExcel(List<Token> tokens, List<LexError> errors, File destination) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            CellStyle headerStyle = createHeaderStyle(workbook);

            Sheet tokenSheet = workbook.createSheet("Tokens");
            Row tokenHeader = tokenSheet.createRow(0);
            writeHeaderCell(tokenHeader, 0, "Token Category", headerStyle);
            writeHeaderCell(tokenHeader, 1, "Lexeme", headerStyle);
            writeHeaderCell(tokenHeader, 2, "Line Number", headerStyle);

            int rowIdx = 1;
            for (Token token : tokens) {
                Row row = tokenSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(token.getCategory());
                row.createCell(1).setCellValue(token.getLexeme());
                row.createCell(2).setCellValue(token.getLineNumber());
            }
            for (int col = 0; col < 3; col++) tokenSheet.autoSizeColumn(col);

            Sheet errorSheet = workbook.createSheet("Errors");
            Row errorHeader = errorSheet.createRow(0);
            writeHeaderCell(errorHeader, 0, "Invalid Lexeme", headerStyle);
            writeHeaderCell(errorHeader, 1, "Line Number", headerStyle);
            writeHeaderCell(errorHeader, 2, "Message", headerStyle);

            rowIdx = 1;
            for (LexError error : errors) {
                Row row = errorSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(error.getInvalidLexeme());
                row.createCell(1).setCellValue(error.getLineNumber());
                row.createCell(2).setCellValue(error.getMessage());
            }
            for (int col = 0; col < 3; col++) errorSheet.autoSizeColumn(col);

            try (FileOutputStream fos = new FileOutputStream(destination)) {
                workbook.write(fos);
            }
        }
    }

    /** Plain-text CSV export (no POI needed) covering the "or .csv" option. */
    public static void exportToCsv(List<Token> tokens, List<LexError> errors, File destination) throws IOException {
        try (FileWriter writer = new FileWriter(destination)) {
            writer.write("Token Category,Lexeme,Line Number\n");
            for (Token token : tokens) {
                writer.write(csvEscape(token.getCategory()) + "," + csvEscape(token.getLexeme()) + "," + token.getLineNumber() + "\n");
            }
            writer.write("\nInvalid Lexeme,Line Number,Message\n");
            for (LexError error : errors) {
                writer.write(csvEscape(error.getInvalidLexeme()) + "," + error.getLineNumber() + "," + csvEscape(error.getMessage()) + "\n");
            }
        }
    }

    private static String csvEscape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            escaped = "\"" + escaped + "\"";
        }
        return escaped;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        style.setFont(boldFont);
        return style;
    }

    private static void writeHeaderCell(Row row, int col, String text, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(text);
        cell.setCellStyle(style);
    }
}
