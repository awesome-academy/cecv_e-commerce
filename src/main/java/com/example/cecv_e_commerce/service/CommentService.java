package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.User;

public interface CommentService {
    CommentDTO addComment(User user, Product product, String content);
}
