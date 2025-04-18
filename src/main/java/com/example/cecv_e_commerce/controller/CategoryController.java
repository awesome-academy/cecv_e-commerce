package com.example.cecv_e_commerce.controller;

import com.example.cecv_e_commerce.service.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.cecv_e_commerce.domain.dto.category.CategoriesResponseDTO;
import com.example.cecv_e_commerce.domain.dto.ApiResponse;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategories() {
        CategoriesResponseDTO categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully", categories));
    }
}
