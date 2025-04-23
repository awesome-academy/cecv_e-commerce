package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.constants.AppConstants;
import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;
import com.example.cecv_e_commerce.domain.model.Comment;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.repository.CommentRepository;
import com.example.cecv_e_commerce.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);
    private final CommentRepository commentRepository;
    private final ModelMapper modelMapper;

    @Override
    public CommentDTO addComment(User user, Product product, String content) {
        logger.debug("Attempting to add comment for Product ID: {} by User ID: {}", product.getId(), user.getId());

        if (commentRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            logger.warn("User ID: {} already commented on product ID: {}", user.getId(), product.getId());
            throw new BadRequestException(AppConstants.MSG_COMMENT_PRODUCT_ERROR);
        }

        Comment comment = new Comment(user, product, content);
        Comment savedComment = commentRepository.save(comment);
        logger.info("Comment saved with ID: {}", savedComment.getId());
        return mapToCommentDTO(savedComment);
    }

    private CommentDTO mapToCommentDTO(Comment comment) {
        if (comment == null) return null;
        CommentDTO dto = modelMapper.map(comment, CommentDTO.class);
        if (comment.getProduct() != null) dto.setProductId(comment.getProduct().getId());
        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setUsername(comment.getUser().getName());
        }
        return dto;
    }
}
