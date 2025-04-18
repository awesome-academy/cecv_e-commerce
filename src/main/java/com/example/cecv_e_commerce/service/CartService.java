package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartResponseDTO;

public interface CartService {
    CartResponseDTO getCart();

    CartResponseDTO addToCart(CartItemRequestCreateDTO cartItemRequestDTO);

    CartResponseDTO removeFromCart(int productId);

    CartResponseDTO updateCartItem(int productId, CartItemRequestUpdateDTO cartItemRequestUpdateDTO);

    CartResponseDTO clearCart();
}
