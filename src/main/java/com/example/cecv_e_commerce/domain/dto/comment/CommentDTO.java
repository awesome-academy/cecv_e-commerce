package com.example.cecv_e_commerce.domain.dto.comment;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Integer id;
    private String content;
    private LocalDateTime createdAt;
    private Integer productId;
    private Integer userId;
    private String username;
}
