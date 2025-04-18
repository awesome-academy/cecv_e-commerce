package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.product.ProductBriefDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface ProductService {
    Page<ProductBriefDTO> getFeaturedProducts(Pageable pageable);
    ProductDetailDTO getProductDetails(Integer productId);
    Page<ProductBriefDTO> searchProducts(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}