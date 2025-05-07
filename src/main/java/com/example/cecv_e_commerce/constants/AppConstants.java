package com.example.cecv_e_commerce.constants;

public final class AppConstants {

    private AppConstants() {}

    // Pagination Defaults
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 6;

    // Search Defaults
    public static final int DEFAULT_SEARCH_PAGE_SIZE = 12;
    public static final String DEFAULT_SORT_FIELD = "createdAt";

    // Error
    public static final String MSG_COMMENT_PRODUCT_ERROR = "You have already commented on this product.";
    public static final String MSG_RATE_PRODUCT_ERROR = "You have already rated this product.";

    // API Messages
    public static final String MSG_FEATURED_PRODUCTS_SUCCESS = "Featured products fetched successfully";
    public static final String MSG_PRODUCT_DETAIL_SUCCESS = "Product details fetched successfully";
    public static final String MSG_PRODUCTS_SEARCH_SUCCESS = "Products searched successfully";
    public static final String MSG_CATEGORIES_LIST_SUCCESS = "Categories fetched successfully";
    public static final String MSG_REVIEW_CREATED_SUCCESS = "Review added successfully";
    public static final String MSG_REVIEWS_FETCHED_SUCCESS = "Product reviews fetched successfully";
    public static final String MSG_REVENUE_STATS_SUCCESS = "Revenue statistics fetched successfully";
    public static final String MSG_BEST_SELLING_PRODUCTS_SUCCESS = "Best selling products fetched successfully";

}
