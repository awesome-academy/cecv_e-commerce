package com.example.cecv_e_commerce.order;

import com.example.cecv_e_commerce.controller.OrderController;
import com.example.cecv_e_commerce.domain.dto.order.*;
import com.example.cecv_e_commerce.domain.dto.product.ProductDTO;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@WebAppConfiguration
@EnableWebMvc
@WebMvcTest
@Import(OrderController.class)
@ContextConfiguration(classes = {OrderService.class})
@WithMockUser(username = "testuser", roles = {"USER"})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRequestDTO orderRequestDTO;
    private OrderResponseDTO orderResponseDTO;
    private OrderItemRequestDeleteDTO orderItemRequestDeleteDTO;
    private OrderItemRequestUpdateDTO orderItemRequestUpdateDTO;
    private OrderPaymentRequestDTO orderPaymentRequestDTO;
    private OrderShippingRequestCreateDTO orderShippingRequestCreateDTO;
    String authenticationToken = "111111";

    @BeforeEach
    void setUp() {
        orderShippingRequestCreateDTO = new OrderShippingRequestCreateDTO();
        orderShippingRequestCreateDTO.setRecipientName("John Doe");
        orderShippingRequestCreateDTO.setRecipientPhone("0123456789");
        orderShippingRequestCreateDTO.setAddressLine1("123 Main St");
        orderShippingRequestCreateDTO.setAddressLine2("Apt 4B");
        orderShippingRequestCreateDTO.setCity("Hanoi");
        orderShippingRequestCreateDTO.setPostalCode("12345");
        orderShippingRequestCreateDTO.setCountry("Vietnam");
        orderShippingRequestCreateDTO.setShippingMethod("STANDARD");
        orderShippingRequestCreateDTO.setShippingFee(10.0);

        OrderItemRequestCreateDTO itemRequest = new OrderItemRequestCreateDTO(1, 2, 50.0, null);

        orderRequestDTO = new OrderRequestDTO(List.of(itemRequest), orderShippingRequestCreateDTO);

        ProductDTO productDTO = new ProductDTO(
                1, "Laptop", "High-performance laptop", new BigDecimal("999.99"), 10
        );
        OrderItemDTO itemDTO = new OrderItemDTO(1, 1, productDTO, 50.0, 2);
        OrderShippingDTO shippingDTO = new OrderShippingDTO(
                1, "John Doe", "0123456789", "123 Main St", "Apt 4B",
                "Hanoi", "12345", "Vietnam", "STANDARD", 10.0
        );
        OrderPaymentDTO paymentDTO = new OrderPaymentDTO(
                1, 1, "CREDIT_CARD", "PAID", 100.0, "TX123",
                LocalDateTime.of(2025, 5, 14, 13, 0)
        );
        orderResponseDTO = new OrderResponseDTO(
                1, 1, List.of(itemDTO), shippingDTO, 1, paymentDTO
        );

        orderItemRequestDeleteDTO = new OrderItemRequestDeleteDTO();
        orderItemRequestDeleteDTO.setOrderId(1);
        orderItemRequestDeleteDTO.setOrderItemId(1);

        orderItemRequestUpdateDTO = new OrderItemRequestUpdateDTO(1, 2, 50.0, 1);

        orderPaymentRequestDTO = new OrderPaymentRequestDTO(
                "CREDIT_CARD",
                "PAID",
                100.0,
                "TX123",
                LocalDateTime.of(2025, 5, 14, 13, 0)
        );
    }

    @Test
    void createOrder_Success() throws Exception {
        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(orderResponseDTO);

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.orderItems[0].id").value(1))
                .andExpect(jsonPath("$.orderItems[0].orderId").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.id").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.name").value("Laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.description").value("High-performance laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.price").value(999.99))
                .andExpect(jsonPath("$.orderItems[0].product.quantity").value(10))
                .andExpect(jsonPath("$.orderItems[0].price").value(50.0))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.orderShipping.recipientName").value("John Doe"))
                .andExpect(jsonPath("$.orderShipping.shippingFee").value(10.0))
                .andExpect(jsonPath("$.numberOfOrders").value(1))
                .andExpect(jsonPath("$.orderPayment.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.orderPayment.paymentAmount").value(100.0));

        verify(orderService, times(1)).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_Failure_NullOrderItems() throws Exception {
        OrderRequestDTO invalidRequest = new OrderRequestDTO(null, orderShippingRequestCreateDTO);

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_Failure_NullOrderShipping() throws Exception {
        OrderRequestDTO invalidRequest = new OrderRequestDTO(List.of(new OrderItemRequestCreateDTO(1, 2, 50.0, null)), null);

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_Failure_InvalidOrderItemProductId() throws Exception {
        OrderItemRequestCreateDTO invalidItem = new OrderItemRequestCreateDTO(null, 2, 50.0, null);
        OrderRequestDTO invalidRequest = new OrderRequestDTO(List.of(invalidItem), orderShippingRequestCreateDTO);

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_Failure_InvalidOrderItemQuantity() throws Exception {
        OrderItemRequestCreateDTO invalidItem = new OrderItemRequestCreateDTO(1, 0, 50.0, null);
        OrderRequestDTO invalidRequest = new OrderRequestDTO(List.of(invalidItem), orderShippingRequestCreateDTO);

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_Failure_InvalidOrderItemPrice() throws Exception {
        OrderItemRequestCreateDTO invalidItem = new OrderItemRequestCreateDTO(1, 2, -1.0, null);
        OrderRequestDTO invalidRequest = new OrderRequestDTO(List.of(invalidItem), orderShippingRequestCreateDTO);

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_Failure_InvalidRecipientName() throws Exception {
        OrderShippingRequestCreateDTO invalidShipping = new OrderShippingRequestCreateDTO();
        invalidShipping.setRecipientName(null);
        invalidShipping.setRecipientPhone("0123456789");
        invalidShipping.setAddressLine1("123 Main St");
        invalidShipping.setAddressLine2("Apt 4B");
        invalidShipping.setCity("Hanoi");
        invalidShipping.setPostalCode("12345");
        invalidShipping.setCountry("Vietnam");
        invalidShipping.setShippingMethod("STANDARD");
        invalidShipping.setShippingFee(10.0);

        OrderRequestDTO invalidRequest = new OrderRequestDTO(
                List.of(new OrderItemRequestCreateDTO(1, 2, 50.0, null)),
                invalidShipping
        );

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void createOrder_Failure_BadRequest() throws Exception {
        when(orderService.createOrder(any(OrderRequestDTO.class)))
                .thenThrow(new BadRequestException("Invalid order data"));

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(orderService, times(1)).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    void getOrderById_Success() throws Exception {
        when(orderService.getOrderById(1)).thenReturn(orderResponseDTO);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.orderItems[0].id").value(1))
                .andExpect(jsonPath("$.orderItems[0].orderId").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.id").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.name").value("Laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.description").value("High-performance laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.price").value(999.99))
                .andExpect(jsonPath("$.orderItems[0].product.quantity").value(10))
                .andExpect(jsonPath("$.orderItems[0].price").value(50.0))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.orderShipping.recipientName").value("John Doe"))
                .andExpect(jsonPath("$.orderShipping.shippingFee").value(10.0))
                .andExpect(jsonPath("$.numberOfOrders").value(1))
                .andExpect(jsonPath("$.orderPayment.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.orderPayment.paymentAmount").value(100.0));

        verify(orderService, times(1)).getOrderById(1);
    }

    @Test
    void getOrderById_Failure_NotFound() throws Exception {
        when(orderService.getOrderById(1))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderById(1);
    }

    @Test
    void getOrderById_Failure_InvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/orders/invalid")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getOrderById(anyInt());
    }

    @Test
    void deleteOrderItem_Success() throws Exception {
        when(orderService.deleteOrderItem(any(OrderItemRequestDeleteDTO.class)))
                .thenReturn(orderResponseDTO);

        mockMvc.perform(delete("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequestDeleteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.orderItems[0].id").value(1))
                .andExpect(jsonPath("$.orderItems[0].orderId").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.id").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.name").value("Laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.description").value("High-performance laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.price").value(999.99))
                .andExpect(jsonPath("$.orderItems[0].product.quantity").value(10))
                .andExpect(jsonPath("$.orderItems[0].price").value(50.0))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.orderShipping.recipientName").value("John Doe"))
                .andExpect(jsonPath("$.orderShipping.shippingFee").value(10.0))
                .andExpect(jsonPath("$.numberOfOrders").value(1))
                .andExpect(jsonPath("$.orderPayment.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.orderPayment.paymentAmount").value(100.0));

        verify(orderService, times(1)).deleteOrderItem(any(OrderItemRequestDeleteDTO.class));
    }

    @Test
    void deleteOrderItem_Failure_NullOrderId() throws Exception {
        OrderItemRequestDeleteDTO invalidRequest = new OrderItemRequestDeleteDTO();
        invalidRequest.setOrderId(null);
        invalidRequest.setOrderItemId(1);

        mockMvc.perform(delete("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).deleteOrderItem(any(OrderItemRequestDeleteDTO.class));
    }

    @Test
    void deleteOrderItem_Failure_NullOrderItemId() throws Exception {
        OrderItemRequestDeleteDTO invalidRequest = new OrderItemRequestDeleteDTO();
        invalidRequest.setOrderId(1);
        invalidRequest.setOrderItemId(null);

        mockMvc.perform(delete("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).deleteOrderItem(any(OrderItemRequestDeleteDTO.class));
    }

    @Test
    void deleteOrderItem_Failure_NotFound() throws Exception {
        when(orderService.deleteOrderItem(any(OrderItemRequestDeleteDTO.class)))
                .thenThrow(new ResourceNotFoundException("Order item not found"));

        mockMvc.perform(delete("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequestDeleteDTO)))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).deleteOrderItem(any(OrderItemRequestDeleteDTO.class));
    }

    @Test
    void updateOrderItem_Success() throws Exception {
        when(orderService.updateOrderItem(any(OrderItemRequestUpdateDTO.class)))
                .thenReturn(orderResponseDTO);

        mockMvc.perform(put("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequestUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.orderItems[0].id").value(1))
                .andExpect(jsonPath("$.orderItems[0].orderId").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.id").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.name").value("Laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.description").value("High-performance laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.price").value(999.99))
                .andExpect(jsonPath("$.orderItems[0].product.quantity").value(10))
                .andExpect(jsonPath("$.orderItems[0].price").value(50.0))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.orderShipping.recipientName").value("John Doe"))
                .andExpect(jsonPath("$.orderShipping.shippingFee").value(10.0))
                .andExpect(jsonPath("$.numberOfOrders").value(1))
                .andExpect(jsonPath("$.orderPayment.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.orderPayment.paymentAmount").value(100.0));

        verify(orderService, times(1)).updateOrderItem(any(OrderItemRequestUpdateDTO.class));
    }

    @Test
    void updateOrderItem_Failure_NullOrderItemId() throws Exception {
        OrderItemRequestUpdateDTO invalidRequest = new OrderItemRequestUpdateDTO(null, 2, 50.0, 1);

        mockMvc.perform(put("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderItem(any(OrderItemRequestUpdateDTO.class));
    }

    @Test
    void updateOrderItem_Failure_InvalidQuantity() throws Exception {
        OrderItemRequestUpdateDTO invalidRequest = new OrderItemRequestUpdateDTO(1, 0, 50.0, 1);

        mockMvc.perform(put("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderItem(any(OrderItemRequestUpdateDTO.class));
    }

    @Test
    void updateOrderItem_Failure_NullPrice() throws Exception {
        OrderItemRequestUpdateDTO invalidRequest = new OrderItemRequestUpdateDTO(1, 2, null, 1);

        mockMvc.perform(put("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderItem(any(OrderItemRequestUpdateDTO.class));
    }

    @Test
    void updateOrderItem_Failure_NullOrderId() throws Exception {
        OrderItemRequestUpdateDTO invalidRequest = new OrderItemRequestUpdateDTO(1, 2, 50.0, null);

        mockMvc.perform(put("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderItem(any(OrderItemRequestUpdateDTO.class));
    }

    @Test
    void updateOrderItem_Failure_NotFound() throws Exception {
        when(orderService.updateOrderItem(any(OrderItemRequestUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Order item not found"));

        mockMvc.perform(put("/api/v1/orders/items")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequestUpdateDTO)))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).updateOrderItem(any(OrderItemRequestUpdateDTO.class));
    }

    @Test
    void updateOrderPayment_Success() throws Exception {
        when(orderService.updateOrderPayment(eq(1), any(OrderPaymentRequestDTO.class)))
                .thenReturn(orderResponseDTO);

        mockMvc.perform(put("/api/v1/orders/1/payment")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderPaymentRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.orderItems[0].id").value(1))
                .andExpect(jsonPath("$.orderItems[0].orderId").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.id").value(1))
                .andExpect(jsonPath("$.orderItems[0].product.name").value("Laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.description").value("High-performance laptop"))
                .andExpect(jsonPath("$.orderItems[0].product.price").value(999.99))
                .andExpect(jsonPath("$.orderItems[0].product.quantity").value(10))
                .andExpect(jsonPath("$.orderItems[0].price").value(50.0))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.orderShipping.recipientName").value("John Doe"))
                .andExpect(jsonPath("$.orderShipping.shippingFee").value(10.0))
                .andExpect(jsonPath("$.numberOfOrders").value(1))
                .andExpect(jsonPath("$.orderPayment.paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$.orderPayment.paymentAmount").value(100.0));

        verify(orderService, times(1)).updateOrderPayment(eq(1), any(OrderPaymentRequestDTO.class));
    }

    @Test
    void updateOrderPayment_Failure_NullPaymentMethod() throws Exception {
        OrderPaymentRequestDTO invalidRequest = new OrderPaymentRequestDTO(
                null, "PAID", 100.0, "TX123", LocalDateTime.now());

        mockMvc.perform(put("/api/v1/orders/1/payment")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderPayment(anyInt(), any(OrderPaymentRequestDTO.class));
    }

    @Test
    void updateOrderPayment_Failure_NullPaymentStatus() throws Exception {
        OrderPaymentRequestDTO invalidRequest = new OrderPaymentRequestDTO(
                "CREDIT_CARD", null, 100.0, "TX123", LocalDateTime.now());

        mockMvc.perform(put("/api/v1/orders/1/payment")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderPayment(anyInt(), any(OrderPaymentRequestDTO.class));
    }

    @Test
    void updateOrderPayment_Failure_InvalidPaymentAmount() throws Exception {
        OrderPaymentRequestDTO invalidRequest = new OrderPaymentRequestDTO(
                "CREDIT_CARD", "PAID", -1.0, "TX123", LocalDateTime.now());

        mockMvc.perform(put("/api/v1/orders/1/payment")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderPayment(anyInt(), any(OrderPaymentRequestDTO.class));
    }

    @Test
    void updateOrderPayment_Failure_NotFound() throws Exception {
        when(orderService.updateOrderPayment(eq(1), any(OrderPaymentRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(put("/api/v1/orders/1/payment")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderPaymentRequestDTO)))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).updateOrderPayment(eq(1), any(OrderPaymentRequestDTO.class));
    }

    @Test
    void updateOrderPayment_Failure_InvalidId() throws Exception {
        mockMvc.perform(put("/api/v1/orders/invalid/payment")
                        .with(csrf())
                        .param("authenticationToken", authenticationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderPaymentRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateOrderPayment(anyInt(), any(OrderPaymentRequestDTO.class));
    }

}
