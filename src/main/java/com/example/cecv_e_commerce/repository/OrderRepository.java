package com.example.cecv_e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cecv_e_commerce.domain.model.Order;
import com.example.cecv_e_commerce.domain.enums.OrderStatusEnum;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status IN :statuses AND o.createdAt >= :startDate AND o.createdAt < :endDate")
    Double findTotalRevenueByStatusInAndCreatedAtBetween(@Param("statuses") List<OrderStatusEnum> statuses,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);
}
