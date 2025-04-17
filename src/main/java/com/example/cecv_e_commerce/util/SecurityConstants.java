package com.example.cecv_e_commerce.util;

public class SecurityConstants {
    public static final String[] PUBLIC_ENDPOINTS = {
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/activate",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password"
    };

    public static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/products/**",
            "/api/v1/categories/**",
            "/api/v1/reviews/**"
    };

    public static final String[] USER_ENDPOINTS = {
            "/api/v1/auth/logout",
            "/api/v1/users/me/**",
            "/api/v1/cart/**",
            "/api/v1/orders/**",
            "/api/v1/suggestions/**"
    };

    public static final String[] ADMIN_ENDPOINTS = {
            "/api/v1/admin/**"
    };
}
