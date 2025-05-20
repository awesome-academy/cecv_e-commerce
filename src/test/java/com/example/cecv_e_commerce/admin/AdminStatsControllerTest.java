package com.example.cecv_e_commerce.admin;

import com.example.cecv_e_commerce.constants.AppConstants;
import com.example.cecv_e_commerce.config.JwtTokenProvider;
import com.example.cecv_e_commerce.config.SecurityConfig;
import com.example.cecv_e_commerce.exception.GlobalExceptionHandler;
import com.example.cecv_e_commerce.controller.admin.AdminStatsController;
import com.example.cecv_e_commerce.domain.dto.stats.BestSellingProductDTO;
import com.example.cecv_e_commerce.domain.dto.stats.RevenueStatsDTO;
import com.example.cecv_e_commerce.service.AdminStatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminStatsController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AdminStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminStatsService adminStatsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Nested
    class GetRevenueStatsTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        void getRevenue_whenMonthlyPeriodAndMonthProvided_shouldReturnOk() throws Exception {
            String periodParam = "monthly";
            int yearParam = 2023;
            Optional<Integer> monthParamOpt = Optional.of(12);
            Integer monthParam = monthParamOpt.get();

            String expectedPeriodDescription = "Monthly: " + monthParam + "/" + yearParam;
            BigDecimal expectedTotalRevenue = new BigDecimal("1500.75");

            RevenueStatsDTO mockStats = new RevenueStatsDTO(
                    expectedPeriodDescription,
                    yearParam,
                    monthParam,
                    expectedTotalRevenue
            );
            when(adminStatsService.getRevenueStats(periodParam, yearParam, monthParamOpt.orElse(null))).thenReturn(mockStats);

            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/revenue")
                    .param("period", periodParam)
                    .param("year", String.valueOf(yearParam))
                    .param("month", String.valueOf(monthParam))
                    .contentType(MediaType.APPLICATION_JSON));

            resultActions
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is(AppConstants.MSG_REVENUE_STATS_SUCCESS)))
                    .andExpect(jsonPath("$.data.periodDescription", is(expectedPeriodDescription)))
                    .andExpect(jsonPath("$.data.year", is(yearParam)))
                    .andExpect(jsonPath("$.data.month", is(monthParam)))
                    .andExpect(jsonPath("$.data.totalRevenue", is(expectedTotalRevenue.doubleValue())));

            verify(adminStatsService, times(1)).getRevenueStats(periodParam, yearParam, monthParamOpt.orElse(null));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getRevenue_whenYearlyPeriodAndMonthNotProvided_shouldReturnOk() throws Exception {
            String periodParam = "yearly";
            int yearParam = 2023;

            String expectedPeriodDescription = "Yearly: " + yearParam;
            BigDecimal expectedTotalRevenue = new BigDecimal("25000.00");

            RevenueStatsDTO mockStats = new RevenueStatsDTO(
                    expectedPeriodDescription,
                    yearParam,
                    null,
                    expectedTotalRevenue
            );
            when(adminStatsService.getRevenueStats(eq(periodParam), eq(yearParam), isNull())).thenReturn(mockStats);

            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/revenue")
                    .param("period", periodParam)
                    .param("year", String.valueOf(yearParam))
                    .contentType(MediaType.APPLICATION_JSON));

            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.periodDescription", is(expectedPeriodDescription)))
                    .andExpect(jsonPath("$.data.year", is(yearParam)))
                    .andExpect(jsonPath("$.data.month").doesNotExist())
                    .andExpect(jsonPath("$.data.totalRevenue", is(expectedTotalRevenue.doubleValue())));

            verify(adminStatsService, times(1)).getRevenueStats(periodParam, yearParam, null);
        }

        @Test
        void getRevenue_whenUserNotAuthenticated_shouldReturnUnauthorized() throws Exception {
            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/revenue")
                    .param("period", "monthly")
                    .param("year", "2023")
                    .contentType(MediaType.APPLICATION_JSON));
            resultActions.andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        void getRevenue_whenUserHasIncorrectRole_shouldReturnForbidden() throws Exception {
            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/revenue")
                    .param("period", "monthly")
                    .param("year", "2023")
                    .contentType(MediaType.APPLICATION_JSON));
            resultActions.andExpect(status().isForbidden());
        }
    }

    @Nested
    class GetBestSellingProductsTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        void getBestSellingProducts_withDefaultParams_shouldReturnOk() throws Exception {
            int defaultLimit = 5;
            String defaultPeriodParam = "all_time";

            List<BestSellingProductDTO> mockProducts = Arrays.asList(
                    new BestSellingProductDTO(1, "Product A", "url_a.jpg", 100L),
                    new BestSellingProductDTO(2, "Product B", "url_b.jpg", 90L)
            );

            when(adminStatsService.getBestSellingProducts(eq(defaultLimit), eq(defaultPeriodParam))).thenReturn(mockProducts);

            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/best-selling-products")
                    .contentType(MediaType.APPLICATION_JSON));

            resultActions
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is(AppConstants.MSG_BEST_SELLING_PRODUCTS_SUCCESS)))
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].productId", is(1)))
                    .andExpect(jsonPath("$.data[0].productName", is("Product A")))
                    .andExpect(jsonPath("$.data[0].imageUrl", is("url_a.jpg")))
                    .andExpect(jsonPath("$.data[0].totalQuantitySold", is(100)))
                    .andExpect(jsonPath("$.data[1].productId", is(2)))
                    .andExpect(jsonPath("$.data[1].productName", is("Product B")))
                    .andExpect(jsonPath("$.data[1].imageUrl", is("url_b.jpg")))
                    .andExpect(jsonPath("$.data[1].totalQuantitySold", is(90)));
            verify(adminStatsService, times(1)).getBestSellingProducts(defaultLimit, defaultPeriodParam);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getBestSellingProducts_withCustomParams_shouldReturnOk() throws Exception {
            int customLimit = 3;
            String customPeriodParam = "last_month";

            List<BestSellingProductDTO> mockProducts = Collections.singletonList(
                    new BestSellingProductDTO(3, "Product C", "url_c.jpg", 50L)
            );

            when(adminStatsService.getBestSellingProducts(customLimit, customPeriodParam)).thenReturn(mockProducts);

            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/best-selling-products")
                    .param("limit", String.valueOf(customLimit))
                    .param("period", customPeriodParam)
                    .contentType(MediaType.APPLICATION_JSON));

            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].productId", is(3)))
                    .andExpect(jsonPath("$.data[0].productName", is("Product C")))
                    .andExpect(jsonPath("$.data[0].imageUrl", is("url_c.jpg")))
                    .andExpect(jsonPath("$.data[0].totalQuantitySold", is(50)));

            verify(adminStatsService, times(1)).getBestSellingProducts(customLimit, customPeriodParam);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getBestSellingProducts_whenLimitIsBelowMin_shouldReturnBadRequest() throws Exception {
            int invalidLimit = 0;
            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/best-selling-products")
                    .param("limit", String.valueOf(invalidLimit))
                    .contentType(MediaType.APPLICATION_JSON));
            resultActions.andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getBestSellingProducts_whenLimitIsAboveMax_shouldReturnBadRequest() throws Exception {
            int invalidLimit = 51;
            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/best-selling-products")
                    .param("limit", String.valueOf(invalidLimit))
                    .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isBadRequest());
        }

        @Test
        void getBestSellingProducts_whenUserNotAuthenticated_shouldReturnUnauthorized() throws Exception {
            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/best-selling-products")
                    .contentType(MediaType.APPLICATION_JSON));
            resultActions.andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER")
        void getBestSellingProducts_whenUserHasIncorrectRole_shouldReturnForbidden() throws Exception {
            ResultActions resultActions = mockMvc.perform(get("/api/v1/admin/stats/best-selling-products")
                    .contentType(MediaType.APPLICATION_JSON));

            resultActions.andExpect(status().isForbidden());
        }
    }
}
