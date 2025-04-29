package com.kwnam.workreport.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class Util {

    public static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        CellType type = cell.getCellType();
        if (type == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        } else if (type == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (type == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (type == CellType.FORMULA) {
            return cell.getCellFormula();
        } else {
            return "";
        }
    }

    public static int parseIntSafe(String str) {
        try {
            return Integer.parseInt(str.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    public static LocalDate parseDate(String str) {
        try {
            return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean parseBoolean(String value) {
        if (value == null) return false;
        value = value.trim().toLowerCase();
        return value.equals("y") || value.equals("yes") || value.equals("true") || value.equals("1") || value.equals("예");
    }

}
