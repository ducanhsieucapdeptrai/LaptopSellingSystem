package com.example.computershop.service.impl;

import com.example.computershop.dto.request.CheckoutRequest;
import com.example.computershop.entity.*;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.service.CheckoutService;
import com.example.computershop.service.NotificationService;
import com.example.computershop.service.OrderService;
import com.example.computershop.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private final OrderService orderService;
    private final VoucherService voucherService;
    private final CartRepository cartRepository;
    private final NotificationService notificationService;

    @Autowired
    public CheckoutServiceImpl(OrderService orderService, VoucherService voucherService, CartRepository cartRepository, NotificationService notificationService) {
        this.orderService = orderService;
        this.voucherService = voucherService;
        this.cartRepository = cartRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Order processCheckout(CheckoutRequest request, User user, List<Cart> cartItems) {
        validateCheckoutData(request, user, cartItems);
        Order order = createOrderWithVoucher(request, cartItems);
        order.setUserId(user.getUserId());
        List<OrderDetail> orderDetails = createOrderDetails(cartItems, order);
        Order savedOrder = orderService.createOrder(order, orderDetails);
        try {
            notificationService.createOrderSuccessNotification(user, savedOrder);
            notificationService.createNewOrderNotificationForAdmin(savedOrder);
        } catch (Exception e) {
            System.err.println("Warning: Failed to create notifications for order " + savedOrder.getId() + ": " + e.getMessage());
        }
        clearUserCart(user);
        savedOrder.setOrderDetails(orderDetails);
        
        return savedOrder;
    }

    @Override
    public Order createOrderWithVoucher(CheckoutRequest request, List<Cart> cartItems) {
        Order order = new Order();
        Long originalAmount = calculateCartOriginalTotal(cartItems);
        Long discountAmount = 0L;
        String voucherCode = null;
        String voucherId = null;
        
        // Get voucher info from cart
        for (Cart item : cartItems) {
            if (item.getVoucherCode() != null && !item.getVoucherCode().isEmpty()) {
                voucherCode = item.getVoucherCode();
                discountAmount += (item.getDiscountAmount() != null ? item.getDiscountAmount() : 0L);
                
                // Get voucher ID
                Optional<Voucher> voucher = voucherService.getVoucherByCode(voucherCode);
                if (voucher.isPresent()) {
                    voucherId = voucher.get().getVoucherId();
                }
                break;
            }
        }
        
        Long finalAmount = originalAmount - discountAmount;

        if (request.getAlternativeReceiverName() != null && !request.getAlternativeReceiverName().trim().isEmpty()) {
            order.setAlternativeReceiverName(request.getAlternativeReceiverName());
        }
        if (request.getAlternativeReceiverPhone() != null && !request.getAlternativeReceiverPhone().trim().isEmpty()) {
            order.setAlternativeReceiverPhone(request.getAlternativeReceiverPhone());
        }

        if (request.getCompleteShippingAddress() != null && !request.getCompleteShippingAddress().trim().isEmpty()) {
            order.setShippingAddress(request.getCompleteShippingAddress());
        }
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNote(request.getNote());
        order.setShippingMethod(request.getShippingMethod());
        order.setShippingFee(request.getShippingFee());
        order.setDistance(request.getDistance());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setOriginalAmount(originalAmount);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(finalAmount + (request.getShippingFee() != null ? request.getShippingFee() : 0L));
        order.setVoucherCode(voucherCode);
        order.setVoucherId(voucherId);
        
        return order;
    }

    @Override
    public List<OrderDetail> createOrderDetails(List<Cart> cartItems, Order order) {
        List<OrderDetail> orderDetails = new ArrayList<>();
        
        for (Cart cartItem : cartItems) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(cartItem.getProduct());
            detail.setVariant(cartItem.getVariant());
            detail.setQuantity(cartItem.getQuantity());
            Long unitPrice;
            if (cartItem.getVariant() != null) {
                unitPrice = cartItem.getVariant().getPrice().longValue();
            } else {
                unitPrice = cartItem.getProduct().getPrice().longValue();
            }
            detail.setUnitPrice(unitPrice);
            orderDetails.add(detail);
        }
        
        return orderDetails;
    }

    @Override
    public Map<String, Long> calculateCheckoutTotals(List<Cart> cartItems) {
        Long subtotal = 0L;
        Long discountTotal = 0L;
        
        for (Cart cart : cartItems) {
            Long itemPrice = cart.getVariant() != null ? 
                cart.getVariant().getPrice().longValue() * cart.getQuantity() :
                cart.getProduct().getPrice().longValue() * cart.getQuantity();
            
            subtotal += itemPrice;
            discountTotal += (cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0L);
        }
        
        Long finalTotal = subtotal - discountTotal;
        
        Map<String, Long> totals = new HashMap<>();
        totals.put("subtotal", subtotal);
        totals.put("discount", discountTotal);
        totals.put("finalTotal", finalTotal);
        
        return totals;
    }

    @Override
    public Long calculateCartOriginalTotal(List<Cart> cartItems) {
        return cartItems.stream()
                .mapToLong(cart -> {
                    if (cart.getVariant() != null) {
                        return cart.getVariant().getPrice().longValue() * cart.getQuantity();
                    } else {
                        return cart.getProduct().getPrice().longValue() * cart.getQuantity();
                    }
                })
                .sum();
    }

    @Override
    public void validateCheckoutData(CheckoutRequest request, User user, List<Cart> cartItems) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        if (request.getPaymentMethod() == null || request.getPaymentMethod().trim().isEmpty()) {
            throw new RuntimeException("Payment method is required");
        }
        
        // Validate stock availability
        for (Cart cart : cartItems) {
            int availableStock = cart.getVariant() != null ? 
                (cart.getVariant().getQuantity() != null ? cart.getVariant().getQuantity() : 0) : 
                (cart.getProduct().getQuantity() != null ? cart.getProduct().getQuantity() : 0);
                
            if (cart.getQuantity() > availableStock) {
                String productName = cart.getProduct().getName();
                throw new RuntimeException(String.format("Insufficient stock for %s. Available: %d, Requested: %d", 
                    productName, availableStock, cart.getQuantity()));
            }
        }
    }

    @Override
    public void clearUserCart(User user) {
        List<Cart> cartItems = cartRepository.findByUser(user);
        cartRepository.deleteAll(cartItems);
    }
} 