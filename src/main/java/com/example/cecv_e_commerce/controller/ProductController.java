package com.example.cecv_e_commerce.controller;

import com.example.cecv_e_commerce.domain.dto.ApiResponse;
import com.example.cecv_e_commerce.domain.dto.product.ProductBriefDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDetailDTO;
import com.example.cecv_e_commerce.domain.dto.product.SearchProductRequestDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewRequestDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewResponseDTO;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.service.ProductService;
import com.example.cecv_e_commerce.service.ReviewCoordinatorService;
import com.example.cecv_e_commerce.constants.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ReviewCoordinatorService reviewCoordinatorService;

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

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ApiResponse> addReview(
            @PathVariable Integer productId,
            @Valid @RequestBody ReviewRequestDTO reviewRequest,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser == null) {
            throw new BadRequestException(AppConstants.MSG_USER_AUTHENTICATION_ERROR);
        }

        ReviewResponseDTO responseData = reviewCoordinatorService.addReviewAndRating(productId, reviewRequest, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AppConstants.MSG_REVIEW_CREATED_SUCCESS, responseData));
    }
 }
