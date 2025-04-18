package com.example.cecv_e_commerce.constants;

import org.springframework.data.domain.Sort;

public final class AppConstants {

    private AppConstants() {}

    // Pagination Defaults
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 6;

    // Search Defaults
    public static final int DEFAULT_SEARCH_PAGE_SIZE = 12;
    public static final String DEFAULT_SORT_FIELD = "createdAt";

    // API Messages
    public static final String MSG_FEATURED_PRODUCTS_SUCCESS = "Featured products fetched successfully";
    public static final String MSG_PRODUCT_DETAIL_SUCCESS = "Product details fetched successfully";
    public static final String MSG_PRODUCTS_SEARCH_SUCCESS = "Products searched successfully";
    public static final String MSG_OPERATION_SUCCESS = "Operation successful";

}
