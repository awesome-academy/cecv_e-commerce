package com.example.cecv_e_commerce.repository;

import com.example.cecv_e_commerce.domain.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Integer> {
    boolean existsByUserIdAndProductId(Integer userId, Integer productId);
}
