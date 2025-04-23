package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.constants.AppConstants;
import com.example.cecv_e_commerce.domain.dto.rating.RatingDTO;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.domain.model.Rating;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.RatingRepository;
import com.example.cecv_e_commerce.repository.ProductRepository;
import com.example.cecv_e_commerce.repository.UserRepository;
import com.example.cecv_e_commerce.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private static final Logger logger = LoggerFactory.getLogger(RatingServiceImpl.class);
    private final RatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public RatingDTO addRating(Integer userId, Integer productId, Integer rating) {
        logger.debug("Attempting to add rating ({}) for Product ID: {} by User ID: {}", rating, productId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id: {} when trying to add rating.", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    logger.warn("Product not found with id: {} when adding rating by user {}", productId, userId);
                    return new ResourceNotFoundException("Product", "id", productId);
                });

        if (ratingRepository.existsByUserIdAndProductId(userId, productId)) {
            logger.warn("User ID: {} already rated product ID: {}", userId, productId);
            throw new BadRequestException(AppConstants.MSG_RATE_PRODUCT_ERROR );
        }

        Rating ratingObj = new Rating(user, product, rating);
        Rating savedRating = ratingRepository.save(ratingObj);
        logger.info("Rating saved with ID: {}", savedRating.getId());
        return mapToRatingDTO(savedRating);
    }

    private RatingDTO mapToRatingDTO(Rating rating) {
        if (rating == null) return null;
        RatingDTO dto = modelMapper.map(rating, RatingDTO.class);
        if (rating.getProduct() != null) dto.setProductId(rating.getProduct().getId());
        if (rating.getUser() != null) {
            dto.setUserId(rating.getUser().getId());
            dto.setUsername(rating.getUser().getName());
        }
        return dto;
    }
}
