package com.example.computershop.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.computershop.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    @Query("SELECT u.userId, u.fullName, COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
            "FROM User u LEFT JOIN u.orders o " +
            "GROUP BY u.userId, u.fullName " +
            "ORDER BY COALESCE(SUM(o.totalAmount), 0) DESC")
    List<Object[]> getUserByRevenueOrder(Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u")
    long countUsers();

    // Khách hàng mới trong khoảng thời gian
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countNewCustomersByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Top khách hàng theo số đơn hàng
    @Query("SELECT u.userId, u.fullName, COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM User u LEFT JOIN u.orders o " +
           "GROUP BY u.userId, u.fullName " +
           "ORDER BY COUNT(o) DESC")
    List<Object[]> getUserByOrderCount(Pageable pageable);

    // Khách hàng mới nhất
    @Query("SELECT u.userId, u.fullName, u.createdAt, COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM User u LEFT JOIN u.orders o " +
           "GROUP BY u.userId, u.fullName, u.createdAt " +
           "ORDER BY u.createdAt DESC")
    List<Object[]> getRecentCustomers(Pageable pageable);
    
    @Override
    List<User> findAll();

    @Override
    Optional<User> findById(String userId);

    @Query("SELECT MONTH(u.createdAt), COUNT(u) FROM User u WHERE YEAR(u.createdAt) = :year GROUP BY MONTH(u.createdAt) ORDER BY MONTH(u.createdAt)")
    List<Object[]> countNewUsersByMonth(@Param("year") int year);
    
    // Methods cho Notification Service
    List<User> findByRole(com.example.computershop.enums.Role role);
    List<User> findByIsActive(Boolean isActive);
}
