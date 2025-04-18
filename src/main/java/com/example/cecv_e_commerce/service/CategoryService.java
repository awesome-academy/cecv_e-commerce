package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.category.CategoryDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoriesResponseDTO;
import com.example.cecv_e_commerce.domain.model.Category;
import com.example.cecv_e_commerce.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoriesResponseDTO getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        List<CategoryDTO> categoryDTOList = categories.stream()
                .map(category -> convertToDTO(category)).collect(Collectors.toList());

        return new CategoriesResponseDTO(categoryDTOList, categoryDTOList.size());
    }

    private CategoryDTO convertToDTO(Category category) {
        return new CategoryDTO(
            category.getId(),
            category.getName(),
            category.getDescription()
        );
    }
}
