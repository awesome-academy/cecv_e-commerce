package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.category.CategoriesResponseDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryResponseDTO;
import com.example.cecv_e_commerce.domain.model.Category;
import com.example.cecv_e_commerce.repository.CategoryRepository;
import com.example.cecv_e_commerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.cecv_e_commerce.exception.BadRequestException;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public CategoriesResponseDTO getAllCategories() {
        logger.debug("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();

        List<CategoryDTO> categoryDTOList =
                categories.stream().map(category -> modelMapper.map(category, CategoryDTO.class))
                        .collect(Collectors.toList());
        logger.info("Found {} categories", categoryDTOList.size());
        return new CategoriesResponseDTO(categoryDTOList, categoryDTOList.size());
    }

    @Override
    @Transactional()
    public CategoryResponseDTO createCategory(CategoryRequestCreateDTO categoryRequestCreateDTO) {
        if (categoryRepository.existsByName(categoryRequestCreateDTO.getName())) {
            throw new BadRequestException("Category name already exists");
        }

        Category category = modelMapper.map(categoryRequestCreateDTO, Category.class);
        categoryRepository.save(category);

        return modelMapper.map(category, CategoryResponseDTO.class);
    }

    @Override
    @Transactional()
    public CategoryResponseDTO updateCategory(Integer id,
            CategoryRequestUpdateDTO categoryRequestUpdateDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getName().equals(categoryRequestUpdateDTO.getName())
                && categoryRepository.existsByName(categoryRequestUpdateDTO.getName())) {
            throw new BadRequestException("Category name already exists");
        }

        category.setName(categoryRequestUpdateDTO.getName());
        category.setDescription(categoryRequestUpdateDTO.getDescription());
        categoryRepository.save(category);

        return modelMapper.map(category, CategoryResponseDTO.class);
    }

    @Override
    @Transactional()
    public void deleteCategory(Integer id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        categoryRepository.deleteById(id);
    }
}
