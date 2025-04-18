package com.example.cecv_e_commerce.repository;

import com.example.cecv_e_commerce.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
