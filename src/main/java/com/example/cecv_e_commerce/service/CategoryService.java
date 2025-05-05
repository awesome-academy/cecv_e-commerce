package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.category.CategoriesResponseDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryResponseDTO;

public interface CategoryService {
    CategoriesResponseDTO getAllCategories();

    CategoryResponseDTO createCategory(CategoryRequestCreateDTO categoryRequestCreateDTO);

    CategoryResponseDTO updateCategory(Integer id,
            CategoryRequestUpdateDTO categoryRequestUpdateDTO);

    void deleteCategory(Integer id);
}
