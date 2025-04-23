package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;

public interface CommentService {
    CommentDTO addComment(Integer userId, Integer productId, String content);
}
