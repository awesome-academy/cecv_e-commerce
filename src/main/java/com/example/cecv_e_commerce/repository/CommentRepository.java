package com.example.cecv_e_commerce.repository;

import com.example.cecv_e_commerce.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    boolean existsByUserIdAndProductId(Integer userId, Integer productId);

    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findByProductId(Integer productId, Pageable pageable);
}
