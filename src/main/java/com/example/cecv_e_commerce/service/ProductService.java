package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.product.ProductBriefDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDetailDTO;
import com.example.cecv_e_commerce.domain.dto.product.SearchProductRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductBriefDTO> getFeaturedProducts(Pageable pageable);
    ProductDetailDTO getProductDetails(Integer productId);
    Page<ProductBriefDTO> searchProducts(SearchProductRequestDTO criteria, Pageable pageable);
}
