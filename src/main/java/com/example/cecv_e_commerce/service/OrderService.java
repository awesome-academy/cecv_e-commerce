package com.example.cecv_e_commerce.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.example.cecv_e_commerce.domain.dto.order.OrderItemRequestDeleteDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderItemRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderPaymentRequestDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderRequestDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderResponseDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderStatusRequestDTO;

@Service
public interface OrderService {
    OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO);

    OrderResponseDTO updateOrderItem(OrderItemRequestUpdateDTO orderItemRequestUpdateDTO);

    OrderResponseDTO deleteOrderItem(OrderItemRequestDeleteDTO orderItemRequestDeleteDTO);

    OrderResponseDTO getOrderById(Integer orderId);

    OrderResponseDTO updateOrderPayment(Integer orderId,
            OrderPaymentRequestDTO orderPaymentRequestDTO);

    OrderResponseDTO updateOrderStatus(Integer orderId,
            OrderStatusRequestDTO orderStatusRequestDTO);

    OrderResponseDTO deleteOrder(Integer orderId);

    Page<OrderResponseDTO> getAllOrders(Pageable pageable, String[] sort, String search);
}
