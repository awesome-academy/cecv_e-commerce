package com.example.cecv_e_commerce.domain.dto.cart;

import java.util.List;

public record CartDTO(Integer id, Integer userId, List<CartItemDTO> items) {
}
