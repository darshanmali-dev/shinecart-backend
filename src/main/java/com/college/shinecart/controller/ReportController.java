package com.college.shinecart.controller;

import com.college.shinecart.dto.ReportDTO;
import com.college.shinecart.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/users")
    public ResponseEntity<List<ReportDTO.UserReportItem>> getUserReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);
        return ResponseEntity.ok(
                reportService.getUserReport(fromDateTime, toDateTime));
    }

    @GetMapping("/orders")
    public ResponseEntity<ReportDTO.OrderReportResponse> getOrderReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return ResponseEntity.ok(
                reportService.getOrderReport(from, to));
    }

    @GetMapping("/products")
    public ResponseEntity<List<ReportDTO.ProductReportItem>> getProductReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        return ResponseEntity.ok(
                reportService.getProductReport(from, to));
    }
}