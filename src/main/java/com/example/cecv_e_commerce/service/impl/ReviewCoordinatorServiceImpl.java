package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;
import com.example.cecv_e_commerce.domain.dto.rating.RatingDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewRequestDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewResponseDTO;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.ProductRepository;
import com.example.cecv_e_commerce.service.CommentService;
import com.example.cecv_e_commerce.service.RatingService;
import com.example.cecv_e_commerce.service.ReviewCoordinatorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewCoordinatorServiceImpl implements ReviewCoordinatorService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewCoordinatorServiceImpl.class);

    private final ProductRepository productRepository;
    private final CommentService commentService;
    private final RatingService ratingService;

    @Override
    @Transactional
    public ReviewResponseDTO addReviewAndRating(Integer productId, ReviewRequestDTO request, User currentUser) {
        logger.info("Coordinating review add for product ID: {} by user ID: {}", productId, currentUser.getId());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.warn("Product not found with id: {} during review coordination by user {}", productId, currentUser.getId());
                    return new ResourceNotFoundException("Product", "id", productId);
                });

        CommentDTO commentDTO = commentService.addComment(currentUser, product, request.getComment());

        RatingDTO ratingDTO = ratingService.addRating(currentUser, product, request.getRating());

        return new ReviewResponseDTO(commentDTO, ratingDTO);
    }
}
