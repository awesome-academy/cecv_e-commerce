package com.example.cecv_e_commerce.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.example.cecv_e_commerce.service.OrderService;
import jakarta.validation.Valid;
import com.example.cecv_e_commerce.domain.dto.order.OrderResponseDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderStatusRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController extends AdminController {
    private final OrderService orderService;

    @GetMapping
    public Page<OrderResponseDTO> getAllOrders(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String search) {
        return orderService.getAllOrders(PageRequest.of(page, size), sort, search);
    }

    @GetMapping("/{orderId}")
    public OrderResponseDTO getOrderById(@PathVariable Integer orderId) {
        return orderService.getOrderById(orderId);
    }

    @PutMapping("/{orderId}/status")
    public OrderResponseDTO updateOrderStatus(@PathVariable Integer orderId,
            @Valid @RequestBody OrderStatusRequestDTO orderStatusRequestDTO) {
        return orderService.updateOrderStatus(orderId, orderStatusRequestDTO);
    }

    @DeleteMapping("/{orderId}")
    public OrderResponseDTO deleteOrder(@PathVariable Integer orderId) {
        return orderService.deleteOrder(orderId);
    }
}
