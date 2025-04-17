package com.example.cecv_e_commerce.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "activation_token"),
        @UniqueConstraint(columnNames = "password_reset_token")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, columnDefinition = "ENUM('GUEST', 'USER', 'ADMIN') DEFAULT 'USER'")
    private Role role = Role.USER;

    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isActive = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "activation_token", unique = true)
    private String activationToken;

    @Column(name = "activation_deadline")
    private LocalDateTime activationDeadline;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "password_reset_token", unique = true)
    private String passwordResetToken;

    @Column(name = "password_reset_deadline")
    private LocalDateTime passwordResetDeadline;

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.isActive = false;
        this.role = Role.USER;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Prefix ROLE_ is automatically added by Spring Security's RoleVoter
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        // Use email as the username for authentication
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}
