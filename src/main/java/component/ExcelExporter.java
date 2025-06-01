
package component;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ExcelExporter {
    
    public static void exportTableModelToExcel(DefaultTableModel model, File file) throws IOException {
        // Create workbook and worksheet
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Laporan Data");
        
        // Create styles
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        
        int currentRow = 0;
        
        // Add title
        HSSFRow titleRow = sheet.createRow(currentRow++);
        HSSFCell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("LAPORAN DATA");
        titleCell.setCellStyle(titleStyle);
        
        // Merge title cells
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, model.getColumnCount() - 1));
        
        // Add date and time
        currentRow++; // Empty row
        HSSFRow dateRow = sheet.createRow(currentRow++);
        HSSFCell dateCell = dateRow.createCell(0);
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        dateCell.setCellValue("Tanggal Export: " + currentDateTime);
        dateCell.setCellStyle(dataStyle);
        
        currentRow++; // Empty row
        
        // Add headers
        HSSFRow headerRow = sheet.createRow(currentRow++);
        for (int col = 0; col < model.getColumnCount(); col++) {
            HSSFCell headerCell = headerRow.createCell(col);
            headerCell.setCellValue(model.getColumnName(col));
            headerCell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        for (int row = 0; row < model.getRowCount(); row++) {
            HSSFRow dataRow = sheet.createRow(currentRow++);
            
            for (int col = 0; col < model.getColumnCount(); col++) {
                HSSFCell dataCell = dataRow.createCell(col);
                Object value = model.getValueAt(row, col);
                
                if (value != null) {
                    String valueStr = value.toString();
                    
                    // Handle different data types
                    if (col == 0) { // No column - number
                        try {
                            dataCell.setCellValue(Integer.parseInt(valueStr));
                            dataCell.setCellStyle(numberStyle);
                        } catch (NumberFormatException e) {
                            dataCell.setCellValue(valueStr);
                            dataCell.setCellStyle(dataStyle);
                        }
                    } else if (col == 4) { // Harga column - keep currency format
                        dataCell.setCellValue(valueStr);
                        dataCell.setCellStyle(dataStyle);
                    } else {
                        dataCell.setCellValue(valueStr);
                        dataCell.setCellStyle(dataStyle);
                    }
                } else {
                    dataCell.setCellValue("");
                    dataCell.setCellStyle(dataStyle);
                }
            }
        }
        
        // Add summary row
        currentRow++; // Empty row
        HSSFRow summaryRow = sheet.createRow(currentRow);
        HSSFCell summaryCell = summaryRow.createCell(0);
        summaryCell.setCellValue("Total Records: " + model.getRowCount());
        summaryCell.setCellStyle(headerStyle);
        
        // Auto-size columns
        for (int col = 0; col < model.getColumnCount(); col++) {
            sheet.autoSizeColumn(col);
            // Add some extra width for better appearance
            int currentWidth = sheet.getColumnWidth(col);
            sheet.setColumnWidth(col, currentWidth + 1000);
        }
        
        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        } finally {
            workbook.close();
        }
    }
    
    private static CellStyle createTitleStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setFontName("Arial");
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private static CellStyle createHeaderStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Arial");
        font.setColor(IndexedColors.BLACK.getIndex());
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Create custom color for #C1EEE5 (light mint green)
        // Method 1: Using a specific unused color index
        HSSFPalette palette = workbook.getCustomPalette();
        short colorIndex = 41; // Use an unused color index
        palette.setColorAtIndex(colorIndex, (byte) 193, (byte) 238, (byte) 229);
        
        style.setFillForegroundColor(colorIndex);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
    
    private static CellStyle createDataStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // Alternate row coloring (this will be applied manually)
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        return style;
    }
    
    private static CellStyle createNumberStyle(HSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        HSSFFont font = workbook.createFont();
        
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // Add borders
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
}

