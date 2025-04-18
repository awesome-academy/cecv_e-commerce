package com.example.cecv_e_commerce.controller;

import com.example.cecv_e_commerce.domain.dto.ApiResponse;
import com.example.cecv_e_commerce.domain.dto.product.ProductBriefDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDetailDTO;
import com.example.cecv_e_commerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // GET /api/v1/products/featured
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProductBriefDTO> featuredProducts = productService.getFeaturedProducts(pageable);
        return ResponseEntity.ok(ApiResponse.success("Featured products fetched successfully", featuredProducts));
    }

    // GET /api/v1/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductDetails(@PathVariable Integer id) {
        ProductDetailDTO productDetails = productService.getProductDetails(id);
        return ResponseEntity.ok(ApiResponse.success("Product details fetched successfully", productDetails));
    }

    // GET /api/v1/products
    @GetMapping
    public ResponseEntity<ApiResponse> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductBriefDTO> productPage = productService.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success("Products searched successfully", productPage));
    }
}