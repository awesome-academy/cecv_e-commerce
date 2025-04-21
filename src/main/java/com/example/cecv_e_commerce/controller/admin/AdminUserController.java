package com.example.cecv_e_commerce.controller.admin;

import com.example.cecv_e_commerce.domain.dto.ApiResponse;
import com.example.cecv_e_commerce.domain.dto.user.UserDTO;
import com.example.cecv_e_commerce.domain.dto.user.UserStatusUpdateDTO;
import com.example.cecv_e_commerce.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String search) {
        Sort.Direction direction = Sort.Direction.fromString(sort.length > 1 ? sort[1] : "asc");
        Sort sorting = Sort.by(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<UserDTO> userPage = adminUserService.getAllUsers(pageable, search);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userPage));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Integer userId) {
        UserDTO userDTO = adminUserService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User details retrieved successfully", userDTO));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody UserStatusUpdateDTO statusUpdateDto) {
        UserDTO updatedUser = adminUserService.updateUserStatus(userId, statusUpdateDto.getIsActive());
        String message = statusUpdateDto.getIsActive() ? "User activated successfully" : "User deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, updatedUser));
    }
}
