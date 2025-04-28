package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.constants.AppConstants;
import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;
import com.example.cecv_e_commerce.domain.model.Comment;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.CommentRepository;
import com.example.cecv_e_commerce.repository.ProductRepository;
import com.example.cecv_e_commerce.repository.UserRepository;
import com.example.cecv_e_commerce.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);
    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public CommentDTO addComment(Integer userId, Integer productId, String content) {
        logger.debug("Attempting to add comment for Product ID: {} by User ID: {}", productId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id: {} when trying to add comment.", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.warn("Product not found with id: {} when adding comment by user {}", productId, userId);
                    return new ResourceNotFoundException("Product", "id", productId);
                });

        if (commentRepository.existsByUserIdAndProductId(userId, productId)) {
            logger.warn("User ID: {} already commented on product ID: {}", userId, productId);
            throw new BadRequestException(AppConstants.MSG_COMMENT_PRODUCT_ERROR);
        }

        Comment comment = new Comment(user, product, content);
        Comment savedComment = commentRepository.save(comment);
        logger.info("Comment saved with ID: {}", savedComment.getId());
        return mapToCommentDTO(savedComment);
    }

    @Override
    public Page<CommentDTO> getCommentsByProductId(Integer productId, Pageable pageable) {
        logger.debug("Fetching comments for product ID: {} with pageable: {}", productId, pageable);
        if (!productRepository.existsById(productId)) {
            logger.warn("Attempted to fetch comments for non-existent product ID: {}", productId);
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        Page<Comment> commentPage = commentRepository.findByProductId(productId, pageable);
        logger.info("Found {} comments for product ID: {}", commentPage.getTotalElements(), productId);

        List<CommentDTO> commentDTOs = commentPage.getContent().stream()
                .map(this::mapToCommentDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(commentDTOs, pageable, commentPage.getTotalElements());
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
