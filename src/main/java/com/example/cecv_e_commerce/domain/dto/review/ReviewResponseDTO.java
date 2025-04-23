package com.example.cecv_e_commerce.domain.dto.review;

import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;
import com.example.cecv_e_commerce.domain.dto.rating.RatingDTO;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private CommentDTO comment;
    private RatingDTO rating;
}
