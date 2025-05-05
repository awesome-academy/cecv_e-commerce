package com.example.cecv_e_commerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.cecv_e_commerce.service.OrderService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.cecv_e_commerce.domain.dto.order.OrderItemRequestDeleteDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderItemRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderPaymentRequestDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderRequestDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderResponseDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderStatusRequestDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public Page<OrderResponseDTO> getAllOrders(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String search) {
        return orderService.getAllOrders(PageRequest.of(page, size), sort, search);
    }

    @PostMapping
    public OrderResponseDTO createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        return orderService.createOrder(orderRequestDTO);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrderById(@PathVariable Integer orderId) {
        return orderService.getOrderById(orderId);
    }

    @DeleteMapping("/items")
    public OrderResponseDTO deleteOrderItem(
            @Valid @RequestBody OrderItemRequestDeleteDTO orderItemRequestDeleteDTO) {
        return orderService.deleteOrderItem(orderItemRequestDeleteDTO);
    }

    @PutMapping("/items")
    public OrderResponseDTO updateOrderItem(
            @Valid @RequestBody OrderItemRequestUpdateDTO orderItemRequestUpdateDTO) {
        return orderService.updateOrderItem(orderItemRequestUpdateDTO);
    }

    @PutMapping("/{orderId}/payment")
    public OrderResponseDTO updateOrderPayment(@PathVariable Integer orderId,
            @Valid @RequestBody OrderPaymentRequestDTO orderPaymentRequestDTO) {
        return orderService.updateOrderPayment(orderId, orderPaymentRequestDTO);
    }

    @PutMapping("/{orderId}/status")
    public OrderResponseDTO updateOrderStatus(@PathVariable Integer orderId,
            @Valid @RequestBody OrderStatusRequestDTO orderStatusRequestDTO) {
        return orderService.updateOrderStatus(orderId, orderStatusRequestDTO);
    }
}
