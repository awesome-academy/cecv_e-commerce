package com.example.cecv_e_commerce.controller.admin;

import com.example.cecv_e_commerce.domain.dto.ApiResponse;
import com.example.cecv_e_commerce.domain.dto.user.UserDTO;
import com.example.cecv_e_commerce.domain.dto.user.UserStatusUpdateDTO;
import com.example.cecv_e_commerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class UserController extends AdminController{

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDTO> userPage = userService.getAllUsers(pageable, search, sort);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", userPage));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> show(@PathVariable Integer userId) {
        UserDTO userDTO = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success("User details retrieved successfully", userDTO));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable Integer userId,
            @Valid @RequestBody UserStatusUpdateDTO statusUpdateDto) {
        UserDTO updatedUser = userService.updateUserStatus(userId, statusUpdateDto.getIsActive());
        String message = statusUpdateDto.getIsActive() ? "User activated successfully" : "User deactivated successfully";
        return ResponseEntity.ok(ApiResponse.success(message, updatedUser));
    }
}
