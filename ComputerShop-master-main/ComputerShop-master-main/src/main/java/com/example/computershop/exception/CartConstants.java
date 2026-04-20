package com.example.computershop.exception;

public final class CartConstants {
    
    // Private constructor to prevent instantiation
    private CartConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // Web constants
    public static final String REDIRECT_ERROR = "redirect:/error";
    public static final String REDIRECT_CART_VIEW = "redirect:/cart/view";
    public static final String REDIRECT_AUTH_LOGIN = "redirect:/auth/login";
    public static final String ORDER = "order";
    public static final String ERROR = "error";
    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";

    public static final String CART_COUNT = "cartCount";
    
    // View names
    public static final String CART_CHECKOUT_VIEW = "Cart/checkout";
    
    // Error messages
    public static final String MSG_ORDER_NOT_FOUND = "Không tìm thấy đơn hàng!";
    public static final String MSG_PERMISSION_DENIED = "Bạn không có quyền xem đơn hàng này!";
    public static final String MSG_LOGIN_TO_ORDER = "Vui lòng đăng nhập để đặt hàng!";
} 