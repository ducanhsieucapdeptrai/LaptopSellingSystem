package com.example.computershop.repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.computershop.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Override
    Optional<Order> findById(String orderId);
    
    List<Order> findByUserIdOrderByOrderDateDesc(String userId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.product LEFT JOIN FETCH o.user WHERE o.id = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") String orderId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.product LEFT JOIN FETCH o.user WHERE o.userId = :userId ORDER BY o.orderDate DESC")
    List<Order> findByUserWithDetails(@Param("userId") String userId);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user WHERE o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByStatus(@Param("status") String status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.product LEFT JOIN FETCH o.user WHERE o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByStatusWithDetails(@Param("status") String status);
    
    // Find orders by user ID and status (for review checking)
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") String status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.user ORDER BY o.orderDate DESC")
    List<Order> findAllWithUser();
    @Query("SELECT MONTH(o.orderDate), SUM(o.totalAmount) " +
            "FROM Order o WHERE YEAR(o.orderDate) = :year " +
            "GROUP BY MONTH(o.orderDate) ORDER BY MONTH(o.orderDate)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT DISTINCT YEAR(o.orderDate) FROM Order o ORDER BY YEAR(o.orderDate) DESC")
    List<Integer> getDistinctYears();

    @Query("SELECT COUNT(o) FROM Order o")
    long countOrders();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigInteger getTotalRevenue();

    // Thống kê theo khoảng thời gian
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    long countOrdersByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Thống kê đơn hàng theo trạng thái
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatusStatistics();

    // Báo cáo doanh số theo ngày
    @Query("SELECT DATE(o.orderDate), COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(o.orderDate) ORDER BY DATE(o.orderDate)")
    List<Object[]> getDailySalesReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Báo cáo doanh số theo tuần
    @Query("SELECT YEAR(o.orderDate), WEEK(o.orderDate), COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(o.orderDate), WEEK(o.orderDate) ORDER BY YEAR(o.orderDate), WEEK(o.orderDate)")
    List<Object[]> getWeeklySalesReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Báo cáo doanh số theo tháng
    @Query("SELECT YEAR(o.orderDate), MONTH(o.orderDate), COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) ORDER BY YEAR(o.orderDate), MONTH(o.orderDate)")
    List<Object[]> getMonthlySalesReport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MONTH(o.orderDate), COUNT(o) FROM Order o WHERE YEAR(o.orderDate) = :year GROUP BY MONTH(o.orderDate)")
    List<Object[]> countOrdersByMonth(@Param("year") int year);
} 
