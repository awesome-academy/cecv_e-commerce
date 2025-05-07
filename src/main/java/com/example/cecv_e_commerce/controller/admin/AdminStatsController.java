package com.example.cecv_e_commerce.controller.admin;

import com.example.cecv_e_commerce.constants.AppConstants;
import com.example.cecv_e_commerce.domain.dto.ApiResponse;
import com.example.cecv_e_commerce.domain.dto.stats.BestSellingProductDTO;
import com.example.cecv_e_commerce.domain.dto.stats.RevenueStatsDTO;
import com.example.cecv_e_commerce.service.AdminStatsService;
import com.example.cecv_e_commerce.service.impl.UserServiceImpl;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Validated
public class AdminStatsController {

    private final AdminStatsService adminStatsService;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getRevenue(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam int year,
            @RequestParam Optional<Integer> month
    ) {
        if (year < 1970 || year > Year.now().getValue() + 5) {
            throw new com.example.cecv_e_commerce.exception.BadRequestException("Invalid year provided.");
        }
        Integer monthValue = null;
        if ("monthly".equalsIgnoreCase(period)) {
            if (month.isEmpty()) {
                throw new com.example.cecv_e_commerce.exception.BadRequestException("Month parameter is required for monthly period.");
            }
            monthValue = month.get();
            if (monthValue < 1 || monthValue > 12) {
                throw new com.example.cecv_e_commerce.exception.BadRequestException("Month must be between 1 and 12.");
            }
        } else if (month.isPresent()) {
            logger.warn("Month parameter provided for yearly period, it will be ignored.");
        }
        RevenueStatsDTO stats = adminStatsService.getRevenueStats(period, year, monthValue);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_REVENUE_STATS_SUCCESS, stats));
    }

    @GetMapping("/best-selling-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getBestSellingProducts(
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit,
            @RequestParam(required = false, defaultValue = "all_time") String period
    ) {
        List<BestSellingProductDTO> products = adminStatsService.getBestSellingProducts(limit, period);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_BEST_SELLING_PRODUCTS_SUCCESS, products));
    }
}
