package com.example.cecv_e_commerce.repository;

import com.example.cecv_e_commerce.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByActivationToken(String token);

    Optional<User> findByPasswordResetToken(String token);
}
