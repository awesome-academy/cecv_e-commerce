package com.example.cev_e_commerce.cart;

import com.example.cecv_e_commerce.controller.CartController;
import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartItemRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.cart.CartResponseDTO;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static com.example.cev_e_commerce.utils.ModelHelper.createTestCartResponseDTO;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@WebAppConfiguration
@EnableWebMvc
@WebMvcTest
@Import(CartController.class)
@ContextConfiguration(classes = {CartService.class})
@WithMockUser(username = "testuser", roles = {"USER"})
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    String authenticationToken = "111111";

    @Test
    void getCart_Success() throws Exception {
        CartResponseDTO mockResponse = createTestCartResponseDTO();
        when(cartService.getCart()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/cart").with(csrf())
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.numberOfItems").value(2))
                .andExpect(jsonPath("$.items[0].product.name", Matchers.is("Product 1")))
                .andExpect(jsonPath("$.items[1].product.name", Matchers.is("Product 2")));

        verify(cartService, times(1)).getCart();
    }

    @Test
    void addToCart_Success() throws Exception {
        CartItemRequestCreateDTO requestDTO = new CartItemRequestCreateDTO();
        requestDTO.setProductId(1);
        requestDTO.setQuantity(2);
        CartResponseDTO mockResponse = createTestCartResponseDTO();

        when(cartService.addToCart(any(CartItemRequestCreateDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/cart")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)))
                .andExpect(jsonPath("$.items[0].product.id", Matchers.is(1)))
                .andExpect(jsonPath("$.items[1].product.id", Matchers.is(2)))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1));

        verify(cartService, times(1)).addToCart(any(CartItemRequestCreateDTO.class));
    }

    @Test
    void addToCart_MissingProductId() throws Exception {
        CartItemRequestCreateDTO invalidRequest = new CartItemRequestCreateDTO();
        invalidRequest.setQuantity(2);

        mockMvc.perform(post("/api/v1/cart")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addToCart(any(CartItemRequestCreateDTO.class));
    }

    @Test
    void removeFromCart_Success() throws Exception {
        Integer productId = 1;
        CartResponseDTO mockResponse = createTestCartResponseDTO();
        when(cartService.removeFromCart(productId)).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/v1/cart/{productId}", productId)
                        .with(csrf())
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

        verify(cartService, times(1)).removeFromCart(productId);
    }

    @Test
    void removeFromCart_Failure_NullProductId() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/null").with(csrf())
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).removeFromCart(any(Integer.class));
    }

    @Test
    void removeFromCart_Failure_NegativeProductId() throws Exception {
        when(cartService.removeFromCart(-1)).thenThrow(new ResourceNotFoundException("Product ID must be positive"));

        mockMvc.perform(delete("/api/v1/cart/-1").with(csrf())
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeFromCart(-1);
    }

    @Test
    void removeFromCart_MissingCsrfToken() throws Exception {
        Integer productId = 1;
        mockMvc.perform(delete("/api/v1/cart/{productId}", productId)
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isForbidden());

        verify(cartService, never()).removeFromCart(any());
    }

    @Test
    void removeFromCart_Failure_NonExistentProductId() throws Exception {
        when(cartService.removeFromCart(999)).thenThrow(new ResourceNotFoundException("Product not found in cart"));

        mockMvc.perform(delete("/api/v1/cart/999")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isNotFound());

        verify(cartService, times(1)).removeFromCart(999);
    }

    @Test
    void updateCartItem_Success() throws Exception {
        Integer productId = 1;
        CartItemRequestUpdateDTO requestDTO = new CartItemRequestUpdateDTO();
        requestDTO.setQuantity(2);
        CartResponseDTO mockResponse = createTestCartResponseDTO();

        when(cartService.updateCartItem(eq(productId), any(CartItemRequestUpdateDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(put("/api/v1/cart/{productId}", productId)
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

        verify(cartService, times(1)).updateCartItem(eq(productId), any(CartItemRequestUpdateDTO.class));
    }

    @Test
    void updateCartItem_Failure_InvalidQuantity() throws Exception {
        CartItemRequestUpdateDTO request = new CartItemRequestUpdateDTO();
        request.setQuantity(0); // Invalid quantity

        mockMvc.perform(put("/api/v1/cart/1")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(cartService, never()).updateCartItem(any(Integer.class), any(CartItemRequestUpdateDTO.class));
    }

    @Test
    void testClearCart() throws Exception {
        CartResponseDTO mockResponse = createTestCartResponseDTO();
        when(cartService.clearCart()).thenReturn(mockResponse);

        mockMvc.perform(delete("/api/v1/cart")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

        verify(cartService, times(1)).clearCart();
    }

    @Test
    void clearCart_Failure_ServiceException() throws Exception {
        when(cartService.clearCart()).thenThrow(new ResourceNotFoundException("Failed to clear cart"));

        mockMvc.perform(delete("/api/v1/cart")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isNotFound());


        verify(cartService, times(1)).clearCart();
    }
}
