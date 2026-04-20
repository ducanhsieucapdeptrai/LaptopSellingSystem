package com.example.computershop.repository;

import com.example.computershop.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCodeAndIsActiveTrueAndExpirationDateAfter(String code, LocalDateTime now);
    Optional<Voucher> findByCode(String code);
} 