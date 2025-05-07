package com.example.cecv_e_commerce.domain.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsDTO {
    private String periodDescription;
    private int year;
    private Integer month;
    private BigDecimal totalRevenue;
}
