package com.example.cecv_e_commerce.controller;

import com.example.cecv_e_commerce.domain.dto.ApiResponse;
import com.example.cecv_e_commerce.domain.dto.product.ProductBriefDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDetailDTO;
import com.example.cecv_e_commerce.domain.dto.product.SearchProductRequestDTO;
import com.example.cecv_e_commerce.service.ProductService;
import com.example.cecv_e_commerce.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse> getFeaturedProducts(
            @PageableDefault(page = AppConstants.DEFAULT_PAGE_NUMBER,
                    size = AppConstants.DEFAULT_PAGE_SIZE,
                    sort = AppConstants.DEFAULT_SORT_FIELD,
                    direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<ProductBriefDTO> featuredProducts = productService.getFeaturedProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_FEATURED_PRODUCTS_SUCCESS, featuredProducts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductDetails(@PathVariable Integer id) {
        ProductDetailDTO productDetails = productService.getProductDetails(id);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_PRODUCT_DETAIL_SUCCESS, productDetails));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> searchProducts(
            SearchProductRequestDTO criteria,
            @PageableDefault(page = 0,
                    size = AppConstants.DEFAULT_SEARCH_PAGE_SIZE,
                    sort = AppConstants.DEFAULT_SORT_FIELD,
                    direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<ProductBriefDTO> productPage = productService.searchProducts(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(AppConstants.MSG_PRODUCTS_SEARCH_SUCCESS, productPage));
    }
}
