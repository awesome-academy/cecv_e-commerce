package com.example.cecv_e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cecv_e_commerce.domain.model.OrderItem;
import com.example.cecv_e_commerce.domain.enums.OrderStatusEnum;
import com.example.cecv_e_commerce.domain.dto.stats.BestSellingProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    @Query("""
           SELECT new com.example.cecv_e_commerce.domain.dto.stats.BestSellingProductDTO(
               oi.product.id, p.name, p.imageUrl, SUM(oi.quantity)
           )
           FROM OrderItem oi JOIN oi.order o JOIN oi.product p
           WHERE o.status IN :statuses AND o.createdAt >= :startDate AND o.createdAt < :endDate
           GROUP BY oi.product.id, p.name, p.imageUrl
           ORDER BY SUM(oi.quantity) DESC, p.name ASC
           """)
    Page<BestSellingProductDTO> findBestSellingProducts(
            @Param("statuses") List<OrderStatusEnum> statuses,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("""
           SELECT new com.example.cecv_e_commerce.domain.dto.stats.BestSellingProductDTO(
               oi.product.id, p.name, p.imageUrl, SUM(oi.quantity)
           )
           FROM OrderItem oi JOIN oi.order o JOIN oi.product p
           WHERE o.status IN :statuses
           GROUP BY oi.product.id, p.name, p.imageUrl
           ORDER BY SUM(oi.quantity) DESC, p.name ASC
           """)
    Page<BestSellingProductDTO> findAllTimeBestSellingProducts(
            @Param("statuses") List<OrderStatusEnum> statuses,
            Pageable pageable);
}
