package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.product.ProductBriefDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductDetailDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryDTO;
import com.example.cecv_e_commerce.domain.dto.product.SearchProductRequestDTO;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.domain.model.Category;
import com.example.cecv_e_commerce.domain.model.Product;
import com.example.cecv_e_commerce.repository.ProductRepository;
import com.example.cecv_e_commerce.service.ProductService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<ProductBriefDTO> getFeaturedProducts(Pageable pageable) {
        logger.debug("Fetching featured products with pageable: {}", pageable);
        Page<Product> productPage = productRepository.findByFeaturedTrue(pageable);
        List<ProductBriefDTO> dtos = productPage.getContent().stream()
                .map(this::mapToBriefDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, productPage.getTotalElements());
    }

    @Override
    public ProductDetailDTO getProductDetails(Integer productId) {
        logger.debug("Fetching product details for id: {}", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return mapToDetailDTO(product);
    }

    @Override
    public Page<ProductBriefDTO> searchProducts(SearchProductRequestDTO criteria, Pageable pageable) {
        logger.debug("Searching products with criteria - keyword: '{}', categoryId: {}, minPrice: {}, maxPrice: {}, pageable: {}",
                criteria.getKeyword(), criteria.getCategoryId(), criteria.getMinPrice(), criteria.getMaxPrice(), pageable);
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("category", JoinType.LEFT);
            }

            if (StringUtils.hasText(criteria.getKeyword())) {
                String likePattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("name").as(String.class)), likePattern);
                Predicate descriptionPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description").as(String.class)), likePattern);
                predicates.add(criteriaBuilder.or(namePredicate, descriptionPredicate));
            }

            if (criteria.getCategoryId() != null) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(categoryJoin.get("id"), criteria.getCategoryId()));
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }

            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Product> productPage = productRepository.findAll(spec, pageable);
        logger.debug("Found {} products matching criteria.", productPage.getTotalElements());
        List<ProductBriefDTO> dtos = productPage.getContent().stream()
                .map(this::mapToBriefDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, productPage.getTotalElements());
    }

    private ProductBriefDTO mapToBriefDTO(Product product) {
        ProductBriefDTO dto = modelMapper.map(product, ProductBriefDTO.class);
        if (product.getCategory() != null) {
            CategoryDTO categoryDTO = modelMapper.map(product.getCategory(), CategoryDTO.class);
            dto.setCategory(categoryDTO);
        } else {
            dto.setCategory(null);
        }
        return dto;
    }

    private ProductDetailDTO mapToDetailDTO(Product product) {
        ProductDetailDTO dto = modelMapper.map(product, ProductDetailDTO.class);
        if (product.getCategory() != null) {
            CategoryDTO categoryDTO = modelMapper.map(product.getCategory(), CategoryDTO.class);
            dto.setCategory(categoryDTO);
        } else {
            dto.setCategory(null);
        }
        return dto;
    }
}
