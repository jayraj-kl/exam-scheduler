package com.exam.scheduler.util;


import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.repository.ExamScheduleRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelExporter {

    @Autowired
    private ExamScheduleRepository scheduleRepository;
    
    public byte[] exportScheduleToExcel(Long scheduleId) {
        ExamSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Exam Schedule");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            createHeaderRow(headerRow, workbook);
            
            // Create data rows
            int rowIndex = 1;
            for (ExamSlot slot : schedule.getExamSlots()) {
                Row row = sheet.createRow(rowIndex++);
                fillDataRow(row, slot);
            }
            
            // Auto size columns
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to ByteArrayOutputStream
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
        
        String[] headers = {
                "Day", "Date", "Faculty Name", "Role", "Subject", "Program", 
                "Exam Type", "Reporting Time", "Room Number", "Students"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }
    
    private void fillDataRow(Row row, ExamSlot slot) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        row.createCell(0).setCellValue(slot.getExamDate().getDayOfWeek().toString());
        row.createCell(1).setCellValue(slot.getExamDate().format(dateFormatter));
        row.createCell(2).setCellValue(slot.getFaculty().getName());
        row.createCell(3).setCellValue("Faculty");
        row.createCell(4).setCellValue(slot.getSubject().getName());
        row.createCell(5).setCellValue(slot.getSubject().getProgram().getName());
        row.createCell(6).setCellValue(slot.getSlotType().toString());
        row.createCell(7).setCellValue(slot.getReportingTime().format(timeFormatter));
        row.createCell(8).setCellValue(slot.getRoom().getRoomNumber());
        row.createCell(9).setCellValue(slot.getStudentCount());
    }
}