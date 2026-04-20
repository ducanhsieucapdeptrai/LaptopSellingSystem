package com.example.computershop.service;

import com.example.computershop.dto.request.CheckoutRequest;
import com.example.computershop.entity.Cart;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.User;

import java.util.List;
import java.util.Map;

public interface CheckoutService {

    Order processCheckout(CheckoutRequest request, User user, List<Cart> cartItems);

    Order createOrderWithVoucher(CheckoutRequest request, List<Cart> cartItems);
    List<OrderDetail> createOrderDetails(List<Cart> cartItems, Order order);

    Map<String, Long> calculateCheckoutTotals(List<Cart> cartItems);

    Long calculateCartOriginalTotal(List<Cart> cartItems);

    void validateCheckoutData(CheckoutRequest request, User user, List<Cart> cartItems);

    void clearUserCart(User user);
} 