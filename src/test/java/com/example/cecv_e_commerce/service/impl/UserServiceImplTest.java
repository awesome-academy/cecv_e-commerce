package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.dto.user.UserDTO;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.exception.ResourceNotFoundException;
import com.example.cecv_e_commerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    private Pageable pageable;
    private Page<User> userPage;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setActive(true);
        user.setActivatedAt(LocalDateTime.now());

        userDTO = new UserDTO();
        userDTO.setId(1);
        userDTO.setName("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setActive(true);

        pageable = PageRequest.of(0, 10, Sort.by("name"));
        List<User> users = Arrays.asList(user);
        userPage = new PageImpl<>(users, pageable, users.size());
    }

    @Test
    void getAllUsers_WithSearchTerm_ShouldReturnFilteredUsers() {
        String searchTerm = "test";
        String[] sort = {"name", "asc"};
        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), any(Pageable.class))).thenReturn(userPage);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        Page<UserDTO> result = userService.getAllUsers(pageable, searchTerm, sort);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), any(Pageable.class));
    }

    @Test
    void getAllUsers_WithoutSearchTerm_ShouldReturnAllUsers() {
        String[] sort = {"name", "asc"};
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(modelMapper.map(any(User.class), eq(UserDTO.class))).thenReturn(userDTO);

        Page<UserDTO> result = userService.getAllUsers(pageable, "", sort);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(userDTO.getId(), result.getId());
        assertEquals(userDTO.getName(), result.getName());
        verify(userRepository).findById(1);
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999));
        verify(userRepository).findById(999);
    }

    @Test
    void updateUserStatus_WhenActivatingUser_ShouldUpdateStatusAndSetActivatedAt() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.updateUserStatus(1, true);

        assertNotNull(result);
        assertTrue(result.isActive());
        verify(userRepository).findById(1);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserStatus_WhenDeactivatingUser_ShouldUpdateStatusAndClearTokens() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.updateUserStatus(1, false);

        assertNotNull(result);
        verify(userRepository).findById(1);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserStatus_WhenUserDoesNotExist_ShouldThrowException() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserStatus(999, true));
        verify(userRepository).findById(999);
        verify(userRepository, never()).save(any(User.class));
    }
}
