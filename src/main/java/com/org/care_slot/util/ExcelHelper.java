package com.org.care_slot.util;

import com.org.care_slot.dto.request.SlotCreateRequest;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelHelper {

    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static boolean hasExcelFormat(MultipartFile file) {
        if (file == null) return false;
        String filename = file.getOriginalFilename();
        return filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"));
    }

    public static List<SlotCreateRequest> excelToSlotRequests(InputStream is) {
        try {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<SlotCreateRequest> requests = new ArrayList<>();
            int rowNumber = 0;

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header row
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                // Check if row is empty
                if (isRowEmpty(currentRow)) {
                    continue;
                }

                Cell doctorIdCell = currentRow.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell dateCell = currentRow.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell startTimeCell = currentRow.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell endTimeCell = currentRow.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell roomCell = currentRow.getCell(4, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                if (doctorIdCell == null || dateCell == null || startTimeCell == null || endTimeCell == null) {
                    continue;
                }

                Long doctorId = parseLongCell(doctorIdCell);
                LocalDate date = parseDateCell(dateCell);
                LocalTime startTime = parseTimeCell(startTimeCell);
                LocalTime endTime = parseTimeCell(endTimeCell);
                String roomName = roomCell != null ? parseStringCell(roomCell) : null;

                if (doctorId != null && date != null && startTime != null && endTime != null) {
                    SlotCreateRequest request = SlotCreateRequest.builder()
                            .doctorId(doctorId)
                            .appointmentDate(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .roomName(roomName)
                            .build();
                    requests.add(request);
                }

                rowNumber++;
            }

            workbook.close();
            return requests;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_EXCEL_FILE);
        }
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

    private static Long parseLongCell(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            return Long.parseLong(cell.getStringCellValue().trim());
        }
        return null;
    }

    private static String parseStringCell(Cell cell) {
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return cell.toString().trim();
    }

    private static LocalDate parseDateCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().trim();
            return LocalDate.parse(text, DATE_FORMATTER);
        }
        return null;
    }

    private static LocalTime parseTimeCell(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalTime();
        } else if (cell.getCellType() == CellType.STRING) {
            String text = cell.getStringCellValue().trim();
            if (text.length() == 5) { // e.g. "08:00"
                return LocalTime.parse(text, TIME_FORMATTER);
            } else if (text.length() == 8) { // e.g. "08:00:00"
                return LocalTime.parse(text);
            }
        }
        return null;
    }
}
