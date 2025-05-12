package com.example.cecv_e_commerce.product;

import com.example.cecv_e_commerce.config.JwtTokenProvider;
import com.example.cecv_e_commerce.config.SecurityConfig;
import com.example.cecv_e_commerce.controller.ProductController;
import com.example.cecv_e_commerce.domain.dto.comment.CommentDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductBriefDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDetailDTO;
import com.example.cecv_e_commerce.domain.dto.product.SearchProductRequestDTO;
import com.example.cecv_e_commerce.domain.dto.rating.RatingDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewRequestDTO;
import com.example.cecv_e_commerce.domain.dto.review.ReviewResponseDTO;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.service.CommentService;
import com.example.cecv_e_commerce.service.ProductService;
import com.example.cecv_e_commerce.service.ReviewCoordinatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private ReviewCoordinatorService reviewCoordinatorService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private Integer productId = 1;
    private User testUser;
    private ProductDetailDTO productDetailDTO;
    private ProductBriefDTO productBriefDTO;
    private CommentDTO testCommentDTO;
    private RatingDTO testRatingDTO;
    private ReviewResponseDTO testReviewResponseDTO;

    @BeforeEach
    void setUp(WebApplicationContext context) throws Exception {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testUser = new User();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("testuser@example.com");
        testUser.setRole(com.example.cecv_e_commerce.domain.enums.Role.USER);
        testUser.setActive(true);

        when(jwtTokenProvider.validateToken(anyString())).thenAnswer(invocation -> {
            String token = invocation.getArgument(0);
            return token != null && !token.trim().isEmpty();
        });
        when(jwtTokenProvider.getUsernameFromJWT(anyString())).thenReturn("testuser@example.com");
        when(userDetailsService.loadUserByUsername("testuser@example.com")).thenReturn(testUser);

        productDetailDTO = new ProductDetailDTO();
        productDetailDTO.setId(productId);
        productDetailDTO.setName("Test Product Detail");
        productDetailDTO.setPrice(new BigDecimal("199.99"));
        productDetailDTO.setQuantity(10);

        productBriefDTO = new ProductBriefDTO();
        productBriefDTO.setId(productId);
        productBriefDTO.setName("Test Product Brief");
        productBriefDTO.setPrice(new BigDecimal("99.99"));
        productBriefDTO.setImageUrl("image.jpg");

        testCommentDTO = new CommentDTO();
        testCommentDTO.setId(1);
        testCommentDTO.setContent("Great product!");
        testCommentDTO.setUserId(testUser.getId());
        testCommentDTO.setUsername(testUser.getName());
        testCommentDTO.setProductId(productId);
        testCommentDTO.setCreatedAt(LocalDateTime.now().minusDays(1));

        testRatingDTO = new RatingDTO();
        testRatingDTO.setId(5);
        testRatingDTO.setRating(5);
        testRatingDTO.setUserId(testUser.getId());
        testRatingDTO.setUsername(testUser.getName());
        testRatingDTO.setProductId(productId);
        testRatingDTO.setCreatedAt(LocalDateTime.now().minusHours(2));

        testReviewResponseDTO = new ReviewResponseDTO(testCommentDTO, testRatingDTO);
    }

    @Test
    void getFeaturedProducts_shouldReturnPageOfProducts() throws Exception {
        PageImpl<ProductBriefDTO> mockPage = new PageImpl<>(Collections.singletonList(productBriefDTO));
        when(productService.getFeaturedProducts(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/products/featured")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product Brief"));
    }

    @Test
    void getProductDetails_shouldReturnProductDetail() throws Exception {
        when(productService.getProductDetails(anyInt())).thenReturn(productDetailDTO);

        mockMvc.perform(get("/api/v1/products/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Product Detail"));
    }

    @Test
    void searchProducts_shouldReturnProductPage() throws Exception {
        PageImpl<ProductBriefDTO> mockPage = new PageImpl<>(Collections.singletonList(productBriefDTO));
        when(productService.searchProducts(any(SearchProductRequestDTO.class), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/products")
                        .param("criteria", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Test Product Brief"));
    }

    @Test
    void getReviewsForProduct_shouldReturnCommentsPage() throws Exception {
        PageImpl<CommentDTO> mockPage = new PageImpl<>(Collections.singletonList(testCommentDTO));
        when(commentService.getCommentsByProductId(anyInt(), any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/products/{productId}/reviews", 1)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].content").value("Great product!"));
    }

    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    @Test
    void addReview_shouldReturnCreatedReview() throws Exception {
        ReviewRequestDTO requestDTO = new ReviewRequestDTO();
        requestDTO.setRating(5);
        requestDTO.setComment("Excellent product!");

        when(reviewCoordinatorService.addReviewAndRating(eq(productId), any(ReviewRequestDTO.class), any(User.class)))
                .thenReturn(testReviewResponseDTO);

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @WithMockUser(username = "testuser@example.com", roles = {"GUEST"})
    @Test
    void addReview_shouldFailWithForbidden_whenUserRoleIsInvalid() throws Exception {
        ReviewRequestDTO reviewRequest = new ReviewRequestDTO();
        reviewRequest.setRating(5);
        reviewRequest.setComment("Nice!");

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addReview_shouldFailWithUnauthorized_whenNoUserLoggedIn() throws Exception {
        ReviewRequestDTO reviewRequest = new ReviewRequestDTO();
        reviewRequest.setRating(5);
        reviewRequest.setComment("Awesome!");

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isUnauthorized());
    }

    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    @Test
    void addReview_shouldFailWithBadRequest_whenInputIsInvalid() throws Exception {
        ReviewRequestDTO reviewRequest = new ReviewRequestDTO();
        reviewRequest.setRating(0);
        reviewRequest.setComment("");

        mockMvc.perform(post("/api/v1/products/{productId}/reviews", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isBadRequest());
    }
}
