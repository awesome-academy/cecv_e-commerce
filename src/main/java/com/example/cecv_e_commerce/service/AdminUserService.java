package com.example.cecv_e_commerce.service;

import com.example.cecv_e_commerce.domain.dto.user.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<UserDTO> getAllUsers(Pageable pageable, String searchTerm, String[] sort);

    UserDTO getUserById(Integer userId);

    UserDTO updateUserStatus(Integer userId, boolean isActive);
}
