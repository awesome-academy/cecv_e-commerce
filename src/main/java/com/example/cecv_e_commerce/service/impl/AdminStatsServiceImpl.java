package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.stats.BestSellingProductDTO;
import com.example.cecv_e_commerce.domain.dto.stats.RevenueStatsDTO;
import com.example.cecv_e_commerce.domain.enums.OrderStatusEnum;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.repository.OrderItemRepository;
import com.example.cecv_e_commerce.repository.OrderRepository;
import com.example.cecv_e_commerce.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private static final Logger logger = LoggerFactory.getLogger(AdminStatsServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private static final List<OrderStatusEnum> VALID_STATS_STATUSES = Arrays.asList(
            OrderStatusEnum.CONFIRMED,
            OrderStatusEnum.SHIPPED,
            OrderStatusEnum.DELIVERED
    );

    @Override
    public RevenueStatsDTO getRevenueStats(String period, int year, Integer month) {
        logger.debug("Calculating revenue stats for period: {}, year: {}, month: {}", period, year, month);

        int currentYear = Year.now().getValue();
        if (year < 1970 || year > currentYear + 5) {
            throw new BadRequestException("Invalid year provided. Year must be between 1970 and " + (currentYear + 5) + ".");
        }

        if ("monthly".equalsIgnoreCase(period)) {
            if (month == null) {
                throw new BadRequestException("Month parameter is required for monthly period.");
            }
            if (month < 1 || month > 12) {
                throw new BadRequestException("Month must be between 1 and 12.");
            }
        } else if (!"yearly".equalsIgnoreCase(period)) {
            throw new BadRequestException("Invalid period parameter. Use 'monthly' or 'yearly'.");
        }

        LocalDateTime startDate;
        LocalDateTime endDate;
        String periodDescription;

        try {
            if ("monthly".equalsIgnoreCase(period)) {
                YearMonth yearMonth = YearMonth.of(year, month);
                startDate = yearMonth.atDay(1).atStartOfDay();
                endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
                periodDescription = String.format("Monthly: %d/%d", month, year);
            } else {
                startDate = LocalDate.of(year, 1, 1).atStartOfDay();
                endDate = LocalDate.of(year + 1, 1, 1).atStartOfDay();
                periodDescription = String.format("Yearly: %d", year);
                month = null;
            }
        } catch (Exception e) {
            logger.error("Error creating date range for stats: period={}, year={}, month={}", period, year, month, e);
            throw new BadRequestException("Invalid date parameters for statistics: " + e.getMessage(), e);
        }

        Double totalRevenueDouble = orderRepository.findTotalRevenueByStatusInAndCreatedAtBetween(
                VALID_STATS_STATUSES, startDate, endDate
        );

        BigDecimal totalRevenue = (totalRevenueDouble == null) ? BigDecimal.ZERO : BigDecimal.valueOf(totalRevenueDouble);

        logger.info("Revenue calculated for {}: {}", periodDescription, totalRevenue);
        return new RevenueStatsDTO(periodDescription, year, month, totalRevenue);
    }

    @Override
    public List<BestSellingProductDTO> getBestSellingProducts(int limit, String period) {
        logger.debug("Fetching best selling products with limit: {} and period: {}", limit, period);

        if (limit <= 0) {
            limit = 5;
            logger.warn("Invalid limit provided for best selling products, using default: {}", limit);
        }

        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();
        boolean isAllTime = (!StringUtils.hasText(period) || "all_time".equalsIgnoreCase(period));

        try {
            if (isAllTime) {
                startDate = LocalDateTime.of(1970, 1, 1, 0, 0);
                endDate = LocalDateTime.of(2999, 1, 1, 0, 0);
                logger.debug("Calculating best selling products for all time");
            } else if ("last_30_days".equalsIgnoreCase(period)) {
                startDate = endDate.minusDays(30).with(LocalTime.MIN);
                endDate = endDate.plusDays(1).with(LocalTime.MIN);
                logger.debug("Calculating best selling products for last 30 days");
            } else if ("monthly".equalsIgnoreCase(period)) {
                startDate = endDate.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
                endDate = startDate.plusMonths(1);
                logger.debug("Calculating best selling products for current month ({})", YearMonth.from(startDate));
            } else if ("yearly".equalsIgnoreCase(period)) {
                startDate = endDate.with(TemporalAdjusters.firstDayOfYear()).with(LocalTime.MIN);
                endDate = startDate.plusYears(1);
                logger.debug("Calculating best selling products for current year ({})", startDate.getYear());
            } else {
                throw new BadRequestException("Invalid period. Use 'monthly', 'yearly', 'last_30_days', or 'all_time'.");
            }
        } catch (Exception e) {
            throw new BadRequestException("Error calculating date range for best selling products period '" + period + "': " + e.getMessage(), e);
        }

        Pageable pageable = PageRequest.of(0, limit);
        Page<BestSellingProductDTO> resultsPage;

        if (isAllTime) {
            resultsPage = orderItemRepository.findAllTimeBestSellingProducts(VALID_STATS_STATUSES, pageable);
        } else {
            resultsPage = orderItemRepository.findBestSellingProducts(VALID_STATS_STATUSES, startDate, endDate, pageable);
        }

        logger.info("Found {} best selling products for period '{}'", resultsPage.getNumberOfElements(), period);
        return resultsPage.getContent();
    }
}
