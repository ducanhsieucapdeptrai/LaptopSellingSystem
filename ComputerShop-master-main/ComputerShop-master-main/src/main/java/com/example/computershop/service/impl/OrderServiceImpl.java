package com.example.computershop.service.impl;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.repository.OrderRepository;
import com.example.computershop.repository.OrderDetailRepository;
import com.example.computershop.service.NotificationService;
import com.example.computershop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Order createOrder(Order order, List<OrderDetail> orderDetails) {
        try {

            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");
            order.setOrderDetails(null);
            
            // Save order first
            Order savedOrder = orderRepository.save(order);
            
            // Save order details
            for (OrderDetail detail : orderDetails) {
                detail.setOrder(savedOrder);
                orderDetailRepository.save(detail);
            }
            return savedOrder;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(String id) {
        return orderRepository.findById(id).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Order getOrderByIdWithDetails(String id) {
        return orderRepository.findByIdWithDetails(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllWithUser();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(String userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserWithDetails(String userId) {
        return orderRepository.findByUserWithDetails(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }
    
    @Override
    @Transactional
    public Order updateOrder(Order order,String oldStatus) {
        System.out.println("=== UPDATE ORDER DEBUG ===");
        System.out.println("Order ID: " + order.getId());
        System.out.println("New Status: " + order.getStatus());
        
        Order existingOrder = orderRepository.findById(order.getId()).orElse(null);
        
        if (existingOrder != null) {
            System.out.println("Old Status in DB: " + oldStatus);
        } else {
            System.out.println("Order not found in DB (new order)");
        }
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        System.out.println("Order saved successfully");
        
        // Gửi notification nếu status thay đổi
        if (oldStatus != null && !oldStatus.equals(order.getStatus())) {
            System.out.println("Status changed: '" + oldStatus + "' -> '" + order.getStatus() + "'");
            try {
                notificationService.createOrderStatusChangeNotification(savedOrder, oldStatus, order.getStatus());
                System.out.println("✅ Notification created successfully");
            } catch (Exception e) {
                System.err.println("❌ Error creating notification: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (oldStatus != null) {
            System.out.println("Status not changed, no notification sent");
        }
        
        System.out.println("=== END UPDATE ORDER ===");
        return savedOrder;
    }
     @Override
    public List<Object[]> getMonthlyRevenue(int year) {
        return orderRepository.getMonthlyRevenue(year);
    }

    @Override
    public List<Integer> getDistinctYears() {
        return orderRepository.getDistinctYears();
    }

    @Override
    public long countOrders() {
        return orderRepository.countOrders();
    }

    @Override
    public BigInteger getTotalRevenue() {
        return orderRepository.getTotalRevenue();
    }

    @Override
    public List<Object[]> getMonthlyOrderCount(int year) {
        return orderRepository.countOrdersByMonth(year);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderId, String newStatus) {
    }
} 
