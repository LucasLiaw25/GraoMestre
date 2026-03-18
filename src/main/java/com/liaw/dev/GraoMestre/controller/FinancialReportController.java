package com.liaw.dev.GraoMestre.controller; // Ajuste o pacote conforme a estrutura do seu projeto

import com.liaw.dev.GraoMestre.dto.response.TopProductResponse;
import com.liaw.dev.GraoMestre.enums.TimePeriod;
import com.liaw.dev.GraoMestre.service.FinancialReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financial-reports")
public class FinancialReportController {

    private final FinancialReportService financialReportService;

    @Autowired
    public FinancialReportController(FinancialReportService financialReportService) {
        this.financialReportService = financialReportService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<FinancialReportService.FinancialReport> getFinancialSummary(
            @RequestParam(required = false) TimePeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (period != null && period != TimePeriod.CUSTOM) {
            return ResponseEntity.ok(financialReportService.generateFinancialReport(period));
        } else if (startDate != null && endDate != null) {
            return ResponseEntity.ok(financialReportService.generateFinancialReport(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
        } else {
            // Default para o dia atual se nenhum período ou data for fornecido
            return ResponseEntity.ok(financialReportService.getDailyFinancialSummary());
        }
    }

    @GetMapping("/today-revenue")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<BigDecimal> getTodayRevenue() {
        return ResponseEntity.ok(financialReportService.getTodayRevenue());
    }

    @GetMapping("/today-orders-status")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<Map<String, Integer>> getTodayPendingAndProcessingOrdersCount() {
        return ResponseEntity.ok(financialReportService.getTodayPendingAndProcessingOrdersCount());
    }

    @GetMapping("/product-revenue")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<BigDecimal> getProductRevenueByPeriod(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financialReportService.getProductRevenueByPeriod(productId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
    }

    @GetMapping("/category-revenue")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<BigDecimal> getCategoryRevenueByPeriod(
            @RequestParam Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financialReportService.getCategoryRevenueByPeriod(categoryId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
    }

    @GetMapping("/product-quantity-sold")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<Integer> getProductQuantitySoldByPeriod(
            @RequestParam Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financialReportService.getProductQuantitySoldByPeriod(productId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
    }

    @GetMapping("/category-quantity-sold")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<Integer> getCategoryQuantitySoldByPeriod(
            @RequestParam Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(financialReportService.getCategoryQuantitySoldByPeriod(categoryId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
    }

    @GetMapping("/top-products-by-revenue")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<TopProductResponse>> getTopNProductsByRevenue(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate != null 
            ? startDate.atStartOfDay() 
            : LocalDate.now().atStartOfDay();
        LocalDateTime end = endDate != null 
            ? endDate.atTime(23, 59, 59) 
            : LocalDate.now().atTime(23, 59, 59);


        return ResponseEntity.ok(
                financialReportService.getTopNProductsByRevenue(limit, start, end)
        );
    }

    @GetMapping("/top-products-by-quantity")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<List<TopProductResponse>> getTopNProductsByQuantitySold(
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate != null 
            ? startDate.atStartOfDay() 
            : LocalDate.now().atStartOfDay();
        LocalDateTime end = endDate != null 
            ? endDate.atTime(23, 59, 59) 
            : LocalDate.now().atTime(23, 59, 59);

        return ResponseEntity.ok(
            financialReportService.getTopNProductsByQuantitySold(limit, start, end)
        );
    }


    @GetMapping("/average-order-value")
    @PreAuthorize("hasAnyAuthority('SCOPE_ADMIN', 'SCOPE_USER')")
    public ResponseEntity<BigDecimal> getAverageOrderValue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate != null 
            ? startDate.atStartOfDay() 
            : LocalDate.now().atStartOfDay();
        LocalDateTime end = endDate != null 
            ? endDate.atTime(23, 59, 59) 
            : LocalDate.now().atTime(23, 59, 59);

        return ResponseEntity.ok(
            financialReportService.getAverageOrderValue(start, end)
        );
    }
}