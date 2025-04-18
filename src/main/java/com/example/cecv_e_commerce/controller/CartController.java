package com.example.cecv_e_commerce.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartResponseDTO;
import com.example.cecv_e_commerce.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponseDTO getCart() {
        return cartService.getCart();
    }

    @PostMapping("/items")
    public CartResponseDTO addToCart(@Valid @RequestBody CartItemRequestCreateDTO cartItemRequestDTO) {
        return cartService.addToCart(cartItemRequestDTO);
    }

    @DeleteMapping("/items/{productId}")
    public CartResponseDTO removeFromCart(@PathVariable Integer productId) {
        return cartService.removeFromCart(productId);
    }

    @PutMapping("/items/{productId}")
    public CartResponseDTO updateCartItem(@PathVariable Integer productId,
        @Valid @RequestBody CartItemRequestUpdateDTO cartItemRequestUpdateDTO) {
        return cartService.updateCartItem(productId, cartItemRequestUpdateDTO);
    }

    @DeleteMapping()
    public CartResponseDTO clearCart() {
        return cartService.clearCart();
    }
}
