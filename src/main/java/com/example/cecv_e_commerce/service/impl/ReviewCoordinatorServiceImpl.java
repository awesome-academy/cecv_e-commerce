package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;
import com.example.cecv_e_commerce.domain.dto.rating.RatingDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewRequestDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewResponseDTO;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.service.CommentService;
import com.example.cecv_e_commerce.service.RatingService;
import com.example.cecv_e_commerce.service.ReviewCoordinatorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.example.cecv_e_commerce.exception.BadRequestException;

@Service
@RequiredArgsConstructor
public class ReviewCoordinatorServiceImpl implements ReviewCoordinatorService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewCoordinatorServiceImpl.class);

    private final CommentService commentService;
    private final RatingService ratingService;

    @Override
    @Transactional
    public ReviewResponseDTO addReviewAndRating(Integer productId, ReviewRequestDTO request, User currentUser) {
        Integer currentUserId = currentUser.getId();
        logger.info("Coordinating review add for product ID: {} by user ID: {}", productId, currentUserId);
        boolean hasRating = request.getRating() != null;
        boolean hasComment = StringUtils.hasText(request.getComment());
        if (!hasRating && !hasComment) {
            throw new BadRequestException("Either rating or comment must be provided.");
        }

        CommentDTO createdComment = null;
        RatingDTO createdRating = null;

        if (hasComment) {
            logger.info("Calling CommentService to add comment for product ID: {} by user ID: {}", productId, currentUserId);
            createdComment = commentService.addComment(currentUserId, productId, request.getComment().trim());
        }

        if (hasRating) {
            logger.info("Calling RatingService to add rating ({}) for product ID: {} by user ID: {}", request.getRating(), productId, currentUserId);
            createdRating = ratingService.addRating(currentUserId, productId, request.getRating());
        }

        logger.info("Review coordination completed for product ID: {} by user ID: {}", productId, currentUserId);
        return new ReviewResponseDTO(createdComment, createdRating);
    }
}
