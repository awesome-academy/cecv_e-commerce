package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.user.UserDTO;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.UserRepository;
import com.example.cecv_e_commerce.service.AdminUserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable, String searchTerm, String[] sort) {
        Sort.Direction direction = Sort.Direction.fromString(sort.length > 1 ? sort[1] : "asc");
        Sort sorting = Sort.by(direction, sort[0]);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sorting);

        logger.debug("Fetching users with pageable: {} and search term: '{}'", sortedPageable, searchTerm);
        Page<User> userPage;

        if (StringUtils.hasText(searchTerm)) {
            userPage = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, sortedPageable);
        } else {
            userPage = userRepository.findAll(sortedPageable);
        }

        return userPage.map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Integer userId) {
        logger.debug("Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    @Transactional
    public UserDTO updateUserStatus(Integer userId, boolean isActive) {
        logger.info("Updating status for user ID: {} to isActive: {}", userId, isActive);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(isActive);

        if (isActive) {
            user.setActivatedAt(LocalDateTime.now());
        } else {
            user.setActivationToken(null);
            user.setPasswordResetToken(null);
            user.setPasswordResetDeadline(null);
            user.setActivatedAt(null);
        }

        User updatedUser = userRepository.save(user);
        logger.info("User status updated successfully for user ID: {}", userId);
        return modelMapper.map(updatedUser, UserDTO.class);
    }
}
