package com.example.cecv_e_commerce.domain.dto.product;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.DecimalMin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestCreateDTO {
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 100, message = "Description must be between 3 and 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    @DecimalMin(value = "0.00", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;
}
