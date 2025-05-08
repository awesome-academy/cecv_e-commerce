package com.example.cecv_e_commerce.controller.admin;

import com.example.cecv_e_commerce.constants.AppConstants;
import com.example.cecv_e_commerce.domain.dto.ApiResponse;
import com.example.cecv_e_commerce.domain.dto.stats.BestSellingProductDTO;
import com.example.cecv_e_commerce.domain.dto.stats.RevenueStatsDTO;
import com.example.cecv_e_commerce.service.AdminStatsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/admin/stats")
@RequiredArgsConstructor
@Validated
public class AdminStatsController extends AdminController {

    private final AdminStatsService adminStatsService;
    private static final Logger logger = LoggerFactory.getLogger(AdminStatsController.class);

    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse> getRevenue(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam int year,
            @RequestParam Optional<Integer> month
    ) {
        logger.info("Received request for revenue stats with period: {}, year: {}, month: {}",
                period, year, month.orElse(null));
        RevenueStatsDTO stats = adminStatsService.getRevenueStats(period, year, month.orElse(null));
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_REVENUE_STATS_SUCCESS, stats));
    }

    @GetMapping("/best-selling-products")
    public ResponseEntity<ApiResponse> getBestSellingProducts(
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int limit,
            @RequestParam(required = false, defaultValue = "all_time") String period
    ) {
        List<BestSellingProductDTO> products = adminStatsService.getBestSellingProducts(limit, period);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_BEST_SELLING_PRODUCTS_SUCCESS, products));
    }
}
