package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentDTO addComment(Integer userId, Integer productId, String content);
    Page<CommentDTO> getCommentsByProductId(Integer productId, Pageable pageable);
}
