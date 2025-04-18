package com.example.cecv_e_commerce.domain.dto.cart;

import com.example.cecv_e_commerce.domain.dto.product.ProductDTO;

public record CartItemDTO(Integer id, int quantity, ProductDTO product) {
}
