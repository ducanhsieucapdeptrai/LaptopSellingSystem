package com.example.computershop.service;

import com.example.computershop.dto.CartItemDisplay;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.User;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.Map;


public interface CartService {

    List<Cart> getCurrentUserCart(Principal principal);

    User getUserFromPrincipal(Principal principal);

    List<CartItemDisplay> convertToDisplayItems(List<Cart> cartItems);
    

    ResponseEntity<Map<String, Object>> addToCart(String productId, String variantId, Integer quantity, Principal principal);
    

    ResponseEntity<Map<String, Object>> updateCartItem(String productId, String variantId, Integer quantity, Principal principal);
    

    ResponseEntity<Map<String, Object>> removeFromCart(String productId, String variantId, Principal principal);
    

    ResponseEntity<Map<String, Object>> getCartCount(Principal principal);

    ResponseEntity<Map<String, Object>> applyVoucher(String voucherCode, Principal principal);

    ResponseEntity<Map<String, Object>> removeVoucher(Principal principal);
    

    Map<String, Long> calculateCartTotals(List<Cart> cartItems);
    

    void clearCart(User user);

    ResponseEntity<Map<String, Object>> createErrorResponse(String message, int status);

    ResponseEntity<Map<String, Object>> createSuccessResponse(Map<String, Object> data);

    String addProductToCart(String productId, int quantity, Principal principal);

    String updateCartQuantity(String productId, int quantity, Principal principal);

    String removeCartItem(String productId, Principal principal);

    String clearUserCart(Principal principal);

    String validateProductAvailability(String productId);

    Object processCartAddition(User user, String productId, int quantity, boolean isAjax);

    String applyVoucherToCart(String voucherCode, Principal principal);

    String removeVoucherFromCart(Principal principal);

    Long calculateCartOriginalTotal(List<Cart> cartItems);

    void distributeDiscountToCartItems(List<Cart> cartItems, Long totalDiscount, String voucherCode);

    void removeVoucherFromAllCartItems(List<Cart> cartItems);

    Map<String, Object> prepareCartViewData(Principal principal);

    Map<String, Object> prepareCheckoutData(Principal principal);

    Map<String, Object> prepareCartReviewData(Principal principal);

    User getUserById(java.util.UUID userId);

    List<Cart> getCurrentUserCartByUserId(java.util.UUID userId);

    void clearCartByUserId(java.util.UUID userId);
} 