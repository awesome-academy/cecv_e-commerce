package com.example.cecv_e_commerce.controller;

import com.example.cecv_e_commerce.service.category.CategoryService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.cecv_e_commerce.domain.dto.category.CategoriesResponseDTO;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<CategoriesResponseDTO> getAllCategories() {
        CategoriesResponseDTO categories = categoryService.getAllCategories();

        return ResponseEntity.ok(categories);
    }
}
