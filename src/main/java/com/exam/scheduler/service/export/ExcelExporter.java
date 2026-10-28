package com.exam.scheduler.service.export;

import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Renders an exam schedule as an .xlsx workbook with one row per slot.
 */
@Service
public class ExcelExporter {

    private static final String[] HEADERS = {
            "Day", "Date", "Faculty Name", "Role", "Subject", "Program",
            "Exam Type", "Reporting Time", "Room Number", "Students"
    };
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] exportScheduleToExcel(ExamSchedule schedule) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exam Schedule");

            createHeaderRow(sheet.createRow(0), workbook);

            int rowIndex = 1;
            for (ExamSlot slot : schedule.getExamSlots()) {
                fillDataRow(sheet.createRow(rowIndex++), slot);
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to export schedule to Excel", e);
        }
    }

    private void createHeaderRow(Row headerRow, Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillDataRow(Row row, ExamSlot slot) {
        row.createCell(0).setCellValue(slot.getExamDate().getDayOfWeek().toString());
        row.createCell(1).setCellValue(slot.getExamDate().format(DATE_FORMAT));
        row.createCell(2).setCellValue(slot.getFaculty().getName());
        row.createCell(3).setCellValue("Faculty");
        row.createCell(4).setCellValue(slot.getSubject().getName());
        row.createCell(5).setCellValue(slot.getSubject().getProgram().getName());
        row.createCell(6).setCellValue(slot.getSlotType().toString());
        row.createCell(7).setCellValue(slot.getReportingTime().format(TIME_FORMAT));
        row.createCell(8).setCellValue(slot.getRoom().getRoomNumber());
        row.createCell(9).setCellValue(slot.getStudentCount());
    }
}
