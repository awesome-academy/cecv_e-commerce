package com.example.cecv_e_commerce.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.cecv_e_commerce.domain.dto.category.CategoryRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.category.CategoryResponseDTO;
import com.example.cecv_e_commerce.service.CategoryService;

@RestController
@RequestMapping("/api/v1/admin/category")
public class AdminCategoryController extends AdminController {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public CategoryResponseDTO createCategory(
            @RequestBody CategoryRequestCreateDTO categoryRequestCreateDTO) {
        return categoryService.createCategory(categoryRequestCreateDTO);
    }

    @PutMapping("/{id}")
    public CategoryResponseDTO updateCategory(@PathVariable Integer id,
            @RequestBody CategoryRequestUpdateDTO categoryRequestUpdateDTO) {
        return categoryService.updateCategory(id, categoryRequestUpdateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
    }
}
