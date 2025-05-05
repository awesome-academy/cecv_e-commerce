package com.example.cecv_e_commerce.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.cecv_e_commerce.domain.dto.order.OrderItemDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderItemRequestDeleteDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderItemRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderPaymentDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderPaymentRequestDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderRequestDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderResponseDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderShippingDTO;
import com.example.cecv_e_commerce.domain.dto.order.OrderStatusRequestDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDTO;
import com.example.cecv_e_commerce.domain.enums.OrderStatusEnum;
import com.example.cecv_e_commerce.domain.model.Order;
import com.example.cecv_e_commerce.domain.model.OrderItem;
import com.example.cecv_e_commerce.domain.model.OrderPayment;
import com.example.cecv_e_commerce.domain.model.OrderShipping;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.OrderRepository;
import com.example.cecv_e_commerce.repository.OrderShippingRepository;
import com.example.cecv_e_commerce.repository.OrderItemRepository;
import com.example.cecv_e_commerce.repository.OrderPaymentRepository;
import com.example.cecv_e_commerce.repository.ProductRepository;
import com.example.cecv_e_commerce.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderShippingRepository orderShippingRepository;
    private final OrderPaymentRepository orderPaymentRepository;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        User user = getCurrentUser();
        Order order = new Order();
        order.setUser(user);

        List<OrderItem> orderItems = orderRequestDTO.orderItems().stream().map(item -> {
            Product product = productRepository.findByIdWithLock(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (product.getQuantity() < item.getQuantity()) {
                throw new BadRequestException(
                        "Not enough product quantity in stock for product ID: " + product.getId()
                                + ". Available: " + product.getQuantity() + ", Requested: "
                                + item.getQuantity());
            }

            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setOrder(order);

            return orderItem;
        }).collect(Collectors.toList());

        OrderShipping orderShipping = new OrderShipping();
        orderShipping.setRecipientName(orderRequestDTO.orderShipping().getRecipientName());
        orderShipping.setRecipientPhone(orderRequestDTO.orderShipping().getRecipientPhone());
        orderShipping.setAddressLine1(orderRequestDTO.orderShipping().getAddressLine1());
        orderShipping.setAddressLine2(orderRequestDTO.orderShipping().getAddressLine2());
        orderShipping.setCity(orderRequestDTO.orderShipping().getCity());
        orderShipping.setPostalCode(orderRequestDTO.orderShipping().getPostalCode());
        orderShipping.setCountry(orderRequestDTO.orderShipping().getCountry());
        orderShipping.setShippingMethod(orderRequestDTO.orderShipping().getShippingMethod());
        orderShipping.setShippingFee(orderRequestDTO.orderShipping().getShippingFee());

        order.setOrderItems(orderItems);
        order.setStatus(OrderStatusEnum.PENDING);
        updateOrderTotal(order);
        orderRepository.save(order);

        orderShipping.setOrder(order);
        orderShippingRepository.save(orderShipping);
        order.setOrderShipping(orderShipping);

        return convertOrderToOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderItem(OrderItemRequestUpdateDTO orderItemRequestUpdateDTO) {
        Order order = findOrderByIdAndCheckAccess(orderItemRequestUpdateDTO.getOrderId());
        OrderItem orderItem =
                findOrderItemByIdAndCheckAccess(orderItemRequestUpdateDTO.getOrderItemId());

        Product product = productRepository.findByIdWithLock(orderItem.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        validateAndUpdateProductQuantity(product, orderItemRequestUpdateDTO.getQuantity(),
                orderItem.getQuantity());

        orderItem.setQuantity(orderItemRequestUpdateDTO.getQuantity());
        orderItem.setPrice(orderItemRequestUpdateDTO.getPrice());
        orderItemRepository.save(orderItem);
        updateOrderTotal(order);

        return convertOrderToOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO deleteOrderItem(OrderItemRequestDeleteDTO orderItemRequestDeleteDTO) {
        Order order = findOrderByIdAndCheckAccess(orderItemRequestDeleteDTO.getOrderId());
        OrderItem orderItem =
                findOrderItemByIdAndCheckAccess(orderItemRequestDeleteDTO.getOrderItemId());

        Product product = productRepository.findByIdWithLock(orderItem.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setQuantity(product.getQuantity() + orderItem.getQuantity());
        productRepository.save(product);

        order.getOrderItems().remove(orderItem);
        updateOrderTotal(order);

        return convertOrderToOrderResponseDTO(order);
    }

    @Override
    public OrderResponseDTO getOrderById(Integer orderId) {
        Order order = findOrderByIdAndCheckAccess(orderId);
        return convertOrderToOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderPayment(Integer orderId,
            OrderPaymentRequestDTO orderPaymentRequestDTO) {
        Order order = findOrderByIdAndCheckAccess(orderId, false);
        if (order.getStatus() == OrderStatusEnum.CANCELLED
                || order.getStatus() == OrderStatusEnum.DELIVERED) {
            throw new BadRequestException("Cannot payment for a cancelled or delivered order");
        }

        OrderPayment orderPayment = order.getOrderPayment();
        if (orderPayment == null) {
            orderPayment = new OrderPayment();
            orderPayment.setOrder(order);
        }

        orderPayment.setPaymentMethod(orderPaymentRequestDTO.getPaymentMethod());
        orderPayment.setPaymentStatus(orderPaymentRequestDTO.getPaymentStatus());
        orderPayment.setPaymentAmount(orderPaymentRequestDTO.getPaymentAmount());
        if (orderPaymentRequestDTO.getTransactionId() != null) {
            orderPayment.setTransactionId(orderPaymentRequestDTO.getTransactionId());
        }
        if (orderPaymentRequestDTO.getPaidAt() != null) {
            orderPayment.setPaidAt(orderPaymentRequestDTO.getPaidAt());
        }

        orderPaymentRepository.save(orderPayment);

        return convertOrderToOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Integer orderId,
            OrderStatusRequestDTO orderStatusRequestDTO) {
        Order order = findOrderByIdAndCheckAccess(orderId);
        order.setStatus(orderStatusRequestDTO.getStatusEnum());
        order.setCancelReason(orderStatusRequestDTO.getReason());
        orderRepository.save(order);

        return convertOrderToOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable, String[] sort, String search) {
        User user = getCurrentUser();

        Specification<Order> spec = (root, query, cb) -> {
            if (!"ADMIN".equalsIgnoreCase(user.getRole().name())) {
                return cb.equal(root.get("user").get("id"), user.getId());
            }

            return cb.conjunction();
        };

        if (search != null && !search.trim().isEmpty()) {
            Specification<Order> searchSpec = (root, query, cb) -> {
                String pattern = "%" + search.toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("orderShipping").get("recipientName")), pattern),
                        cb.like(cb.lower(root.get("orderShipping").get("recipientPhone")), pattern),
                        cb.like(cb.lower(root.get("orderShipping").get("addressLine1")), pattern),
                        cb.like(cb.lower(root.get("orderShipping").get("city")), pattern),
                        cb.like(cb.lower(root.get("status").as(String.class)), pattern));
            };
            spec = spec.and(searchSpec);
        }

        Sort sortSpec;
        if (sort == null || sort.length == 0) {
            sortSpec = Sort.by(Sort.Direction.DESC, "id");
        } else {
            List<Sort.Order> orders = new ArrayList<>();
            for (String sortField : sort) {
                String[] parts = sortField.split(",");
                if (parts.length == 2) {
                    String field = parts[0];
                    String direction = parts[1];
                    orders.add(new Sort.Order(Sort.Direction.fromString(direction), field));
                }
            }
            sortSpec = Sort.by(orders);
        }

        Pageable sortedPageable =
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortSpec);
        return orderRepository.findAll(spec, sortedPageable)
                .map(this::convertOrderToOrderResponseDTO);
    }


    @Override
    @Transactional
    public OrderResponseDTO deleteOrder(Integer orderId) {
        Order order = findOrderByIdAndCheckAccess(orderId);
        orderRepository.delete(order);
        return convertOrderToOrderResponseDTO(order);
    }

    private User getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return user;
    }

    private Order findOrderByIdAndCheckAccess(Integer orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if ("ADMIN".equalsIgnoreCase(user.getRole().name())) {
            return order;
        }

        if (order.getUser().getId() != user.getId()) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (order.getStatus() != OrderStatusEnum.PENDING) {
            throw new BadRequestException("Cannot modify a order is not pending");
        }

        return order;
    }

    private Order findOrderByIdAndCheckAccess(Integer orderId, boolean checkStatus) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getUser().getId() != user.getId()) {
            throw new ResourceNotFoundException("Order not found");
        }

        if (checkStatus && order.getStatus() != OrderStatusEnum.PENDING) {
            throw new BadRequestException("Cannot modify a order is not pending");
        }

        return order;
    }

    private OrderItem findOrderItemByIdAndCheckAccess(Integer orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
    }

    private void updateOrderTotal(Order order) {
        double total = order.getOrderItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        order.setTotal(total);

        orderRepository.save(order);
    }

    private void validateAndUpdateProductQuantity(Product product, int requestedQuantity,
            int currentOrderItemQuantity) {
        if (product.getQuantity() < (requestedQuantity - currentOrderItemQuantity)) {
            throw new BadRequestException(
                    "Not enough product quantity in stock. Available: " + product.getQuantity());
        }
        product.setQuantity(product.getQuantity() - (requestedQuantity - currentOrderItemQuantity));
        productRepository.save(product);
    }

    private OrderResponseDTO convertOrderToOrderResponseDTO(Order order) {
        List<OrderItemDTO> orderItems = order.getOrderItems().stream()
                .map(item -> new OrderItemDTO(item.getId(), order.getId(),
                        new ProductDTO(item.getProduct().getId(), item.getProduct().getName(),
                                item.getProduct().getDescription(), item.getProduct().getPrice(),
                                item.getProduct().getQuantity()),
                        item.getPrice(), item.getQuantity()))
                .collect(Collectors.toList());

        OrderShipping orderShipping = order.getOrderShipping();
        OrderShippingDTO orderShippingDTO = new OrderShippingDTO(orderShipping.getId(),
                orderShipping.getRecipientName(), orderShipping.getRecipientPhone(),
                orderShipping.getAddressLine1(), orderShipping.getAddressLine2(),
                orderShipping.getCity(), orderShipping.getPostalCode(), orderShipping.getCountry(),
                orderShipping.getShippingMethod(), orderShipping.getShippingFee());

        OrderPaymentDTO orderPaymentDTO = null;
        if (order.getOrderPayment() != null) {
            OrderPayment orderPayment = order.getOrderPayment();
            orderPaymentDTO = new OrderPaymentDTO(orderPayment.getId(),
                    orderPayment.getOrder().getId(), orderPayment.getPaymentMethod(),
                    orderPayment.getPaymentStatus(), orderPayment.getPaymentAmount(),
                    orderPayment.getTransactionId(), orderPayment.getPaidAt());
        }

        return new OrderResponseDTO(order.getId(), order.getUser().getId(), orderItems,
                orderShippingDTO, order.getOrderItems().size(), orderPaymentDTO);
    }
}
