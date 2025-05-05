package com.example.cecv_e_commerce.controller.admin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import com.example.cecv_e_commerce.domain.dto.product.ProductRequestCreateDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductRequestUpdateDTO;
import com.example.cecv_e_commerce.domain.dto.product.ProductResponseDTO;
import com.example.cecv_e_commerce.service.ProductService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/admin/products")
public class AdminProductController extends AdminController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ProductResponseDTO createProduct(
            @Validated @RequestBody ProductRequestCreateDTO productRequestCreateDTO) {
        return productService.createProduct(productRequestCreateDTO);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO updateProduct(@PathVariable Integer id,
            @Validated @RequestBody ProductRequestUpdateDTO productRequestUpdateDTO) {
        return productService.updateProduct(id, productRequestUpdateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
    }
}
