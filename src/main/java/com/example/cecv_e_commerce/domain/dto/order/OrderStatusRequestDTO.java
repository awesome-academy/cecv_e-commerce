package com.example.cecv_e_commerce.domain.dto.order;

import com.example.cecv_e_commerce.domain.enums.OrderStatusEnum;
import com.example.cecv_e_commerce.domain.validation.ValidEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusRequestDTO {
    @NotBlank(message = "Status is required")
    @ValidEnum(enumClass = OrderStatusEnum.class, message = "Invalid status value")
    private String status;

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must be less than 255 characters")
    private String reason;

    public OrderStatusEnum getStatusEnum() {
        try {
            return OrderStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
