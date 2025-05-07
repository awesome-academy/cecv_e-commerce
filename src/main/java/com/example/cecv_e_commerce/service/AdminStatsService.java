package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.stats.BestSellingProductDTO;
import com.example.cecv_e_commerce.domain.dto.stats.RevenueStatsDTO;

import java.util.List;

public interface AdminStatsService {
    RevenueStatsDTO getRevenueStats(String period, int year, Integer month);
    List<BestSellingProductDTO> getBestSellingProducts(int limit, String period);
}
