package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.cart.CartItemDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartResponseDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDTO;
import com.example.cecv_e_commerce.domain.model.Cart;
import com.example.cecv_e_commerce.domain.model.CartItem;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.CartRepository;
import com.example.cecv_e_commerce.repository.ProductRepository;
import com.example.cecv_e_commerce.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Cart createCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    public CartResponseDTO getCart() {
        Cart cart = getCurrentUserCart();
        return mapToCartResponseDTO(cart);
    }

    public CartResponseDTO addToCart(CartItemRequestCreateDTO cartItemRequestDTO) {
        Cart cart = getCurrentUserCart();
        Product product = productRepository.findById(cartItemRequestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getQuantity() < cartItemRequestDTO.getQuantity()) {
            throw new BadRequestException(
                    "Not enough stock available. Available quantity: " + product.getQuantity());
        }

        CartItem existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId())).findFirst()
                .orElse(null);

        if (existingCartItem != null) {
            Integer newQuantity = existingCartItem.getQuantity() + cartItemRequestDTO.getQuantity();
            if (newQuantity > product.getQuantity()) {
                throw new BadRequestException(
                        "Not enough stock available. Available quantity: " + product.getQuantity());
            }
            existingCartItem.setQuantity(newQuantity);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemRequestDTO.getQuantity());
            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);

        return mapToCartResponseDTO(cart);
    }

    public CartResponseDTO removeFromCart(Integer productId) {
        Cart cart = getCurrentUserCart();
        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        cartRepository.save(cart);

        return mapToCartResponseDTO(cart);
    }

    public CartResponseDTO updateCartItem(Integer productId,
            CartItemRequestUpdateDTO cartItemRequestUpdateDTO) {
        Cart cart = getCurrentUserCart();
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));

        if (cartItemRequestUpdateDTO.getQuantity() > cartItem.getProduct().getQuantity()) {
            throw new BadRequestException("Not enough stock available. Available quantity: "
                    + cartItem.getProduct().getQuantity());
        }

        cartItem.setQuantity(cartItemRequestUpdateDTO.getQuantity());
        cartRepository.save(cart);

        return mapToCartResponseDTO(cart);
    }

    public CartResponseDTO clearCart() {
        Cart cart = getCurrentUserCart();
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToCartResponseDTO(cart);
    }


    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new ResourceNotFoundException("User not found");
        }
        User user = (User) authentication.getPrincipal();
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return user;
    }

    private Cart getCurrentUserCart() {
        User user = getCurrentUser();
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private CartResponseDTO mapToCartResponseDTO(Cart cart) {
        return new CartResponseDTO(cart.getId(), cart.getUser().getId(),
                cart.getItems().stream().map(this::mapToCartItemDTO).collect(Collectors.toList()),
                cart.getItems().size());
    }

    private CartItemDTO mapToCartItemDTO(CartItem cartItem) {
        Product product = cartItem.getProduct();
        ProductDTO productDTO = new ProductDTO(product.getId(), product.getName(),
                product.getDescription(), product.getPrice(), product.getQuantity());

        return new CartItemDTO(cartItem.getId(), cartItem.getQuantity(), productDTO);
    }
}
