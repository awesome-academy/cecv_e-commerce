package com.example.cecv_e_commerce.repository;

import com.example.cecv_e_commerce.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> { // <<--- ID là Integer

    Page<Product> findByFeaturedTrue(Pageable pageable);
}