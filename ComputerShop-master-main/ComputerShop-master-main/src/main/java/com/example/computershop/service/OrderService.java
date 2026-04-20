package com.example.computershop.service;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import java.math.BigInteger;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order, List<OrderDetail> orderDetails);
    Order getOrderById(String id);
    Order getOrderByIdWithDetails(String id);
    List<Order> getAllOrders();
    List<Order> getOrdersByUser(String userId);
    List<Order> getOrdersByUserWithDetails(String userId);

    List<Order> getOrdersByStatus(String status);
    Order updateOrder(Order order, String oldStatus);
    void updateOrderStatus(String orderId, String newStatus);
    List<Object[]> getMonthlyRevenue(int year);
    List<Integer> getDistinctYears();
    long countOrders();
    BigInteger getTotalRevenue();
    List<Object[]> getMonthlyOrderCount(int year);

}
