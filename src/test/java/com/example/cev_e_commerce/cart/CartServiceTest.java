package com.example.cev_e_commerce.cart;

import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartResponseDTO;
import com.example.cecv_e_commerce.domain.model.Cart;
import com.example.cecv_e_commerce.domain.model.CartItem;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.CartRepository;
import com.example.cecv_e_commerce.repository.ProductRepository;
import com.example.cecv_e_commerce.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        cart = new Cart();
        cart.setId(1);
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        product = new Product();
        product.setId(1);
        product.setName("Test Product");
        product.setDescription("Description");
        product.setPrice(BigDecimal.valueOf(100.0));
        product.setQuantity(10);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null);
        SecurityContextImpl securityContext = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createCart_Success() {
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart createdCart = cartService.createCart(user);

        assertNotNull(createdCart);
        assertEquals(1, createdCart.getId());
        assertEquals(user, createdCart.getUser());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void getCart_Success() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        CartResponseDTO response = cartService.getCart();

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(1, response.userId());
        assertEquals(0, response.numberOfItems());
        assertTrue(response.items().isEmpty());
        verify(cartRepository, times(1)).findByUserId(1);
    }

    @Test
    void getCart_Failure_CartNotFound() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCart();
        });

        assertEquals("Cart not found", exception.getMessage());
        verify(cartRepository, times(1)).findByUserId(1);
    }

    @Test
    void getCart_Failure_UserNotFound() {
        SecurityContextHolder.clearContext(); // Simulate no user

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.getCart();
        });

        assertEquals("User not found", exception.getMessage());
        verify(cartRepository, never()).findByUserId(anyInt());
    }

    @Test
    void addToCart_Success_NewItem() {
        CartItemRequestCreateDTO request = new CartItemRequestCreateDTO();
        request.setProductId(1);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.addToCart(request);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().getFirst().getQuantity());
        verify(productRepository, times(1)).findById(1);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void addToCart_Success_ExistingItem() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);
        cart.getItems().add(cartItem);

        CartItemRequestCreateDTO request = new CartItemRequestCreateDTO();
        request.setProductId(1);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.addToCart(request);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity()); // 3 + 2
        verify(productRepository, times(1)).findById(1);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void addToCart_Failure_ProductNotFound() {
        CartItemRequestCreateDTO request = new CartItemRequestCreateDTO();
        request.setProductId(1);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.addToCart(request);
        });

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).findById(1);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToCart_Failure_NotEnoughStock_NewItem() {
        CartItemRequestCreateDTO request = new CartItemRequestCreateDTO();
        request.setProductId(1);
        request.setQuantity(15); // More than available (10)

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            cartService.addToCart(request);
        });

        assertEquals("Not enough stock available. Available quantity: 10", exception.getMessage());
        verify(productRepository, times(1)).findById(1);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToCart_Failure_NotEnoughStock_ExistingItem() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(8);
        cart.getItems().add(cartItem);

        CartItemRequestCreateDTO request = new CartItemRequestCreateDTO();
        request.setProductId(1);
        request.setQuantity(5); // 8 + 5 = 13 > 10

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            cartService.addToCart(request);
        });

        assertEquals("Not enough stock available. Available quantity: 10", exception.getMessage());
        verify(productRepository, times(1)).findById(1);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void removeFromCart_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.removeFromCart(1);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void removeFromCart_Failure_CartNotFound() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.removeFromCart(1);
        });

        assertEquals("Cart not found", exception.getMessage());
        verify(cartRepository, times(1)).findByUserId(1);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItem_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);

        CartItemRequestUpdateDTO request = new CartItemRequestUpdateDTO();
        request.setQuantity(5);

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.updateCartItem(1, request);

        assertNotNull(response);
        assertEquals(1, response.id());
        assertEquals(5, cart.getItems().getFirst().getQuantity());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void updateCartItem_Failure_ProductNotInCart() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        CartItemRequestUpdateDTO request = new CartItemRequestUpdateDTO();
        request.setQuantity(5);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.updateCartItem(1, request);
        });

        assertEquals("Product not found in cart", exception.getMessage());
        verify(cartRepository, times(1)).findByUserId(1);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void updateCartItem_Failure_NotEnoughStock() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);

        CartItemRequestUpdateDTO request = new CartItemRequestUpdateDTO();
        request.setQuantity(15); // More than available (10)

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            cartService.updateCartItem(1, request);
        });

        assertEquals("Not enough stock available. Available quantity: 10", exception.getMessage());
        verify(cartRepository, times(1)).findByUserId(1);
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void clearCart_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(1);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDTO response = cartService.clearCart();

        assertNotNull(response);
        assertEquals(1, response.id());
        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void clearCart_Failure_CartNotFound() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            cartService.clearCart();
        });

        assertEquals("Cart not found", exception.getMessage());
        verify(cartRepository, times(1)).findByUserId(1);
        verify(cartRepository, never()).save(any(Cart.class));
    }
}
