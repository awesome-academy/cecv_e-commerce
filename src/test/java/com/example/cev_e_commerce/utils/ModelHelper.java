package com.example.cev_e_commerce.utils;

import com.example.cecv_e_commerce.domain.dto.cart.CartItemDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartResponseDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDTO;

import java.math.BigDecimal;
import java.util.List;

public class ModelHelper {

    public static CartResponseDTO createTestCartResponseDTO() {
        ProductDTO product1 = new ProductDTO(1, "Product 1", "",new BigDecimal("100.0"), 1);
        ProductDTO product2 = new ProductDTO(2, "Product 2", "",new BigDecimal("50.0"),2);

        List<CartItemDTO> items = List.of(
                new CartItemDTO(1, 2, product1),
                new CartItemDTO(2, 1, product2)
        );

        return new CartResponseDTO(1, 1, items, items.size());
    }
}
