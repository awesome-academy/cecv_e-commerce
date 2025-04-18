package com.example.cecv_e_commerce.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.cecv_e_commerce.domain.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserId(Integer userId);
}
