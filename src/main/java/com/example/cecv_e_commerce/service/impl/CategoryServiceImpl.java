package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.category.CategoriesResponseDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryDTO;
import com.example.cecv_e_commerce.domain.model.Category;
import com.example.cecv_e_commerce.repository.CategoryRepository;
import com.example.cecv_e_commerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        List<CategoryDTO> categoryDTOList = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
        logger.info("Found {} categories", categoryDTOList.size());
        return new CategoriesResponseDTO(categoryDTOList, categoryDTOList.size());
    }
}
