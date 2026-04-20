package com.example.computershop.service.impl;

import com.example.computershop.entity.Voucher;
import com.example.computershop.repository.VoucherRepository;
import com.example.computershop.service.VoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {
    private static final Logger logger = LoggerFactory.getLogger(VoucherServiceImpl.class);
    private final VoucherRepository voucherRepository;

    @Autowired
    public VoucherServiceImpl(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @Override
    public Voucher createVoucher(Voucher voucher) {
        // Ensure proper initialization before saving
        voucher.ensureProperInitialization();
        return voucherRepository.save(voucher);
    }

    @Override
    public Optional<Voucher> getVoucherByCode(String code) {
        Optional<Voucher> voucher = voucherRepository.findByCode(code);
        if (voucher.isEmpty()) {
            return voucher;
        }
        voucher.get().ensureProperInitialization();
        return voucher;
    }

    @Override
    public Optional<Voucher> getValidVoucherByCode(String code) {
        System.out.println("DEBUG - Searching for valid voucher with code: " + code);
        Optional<Voucher> voucher = voucherRepository.findByCodeAndIsActiveTrueAndExpirationDateAfter(code, LocalDateTime.now());
        if (voucher.isPresent()) {
            Voucher v = voucher.get();
            // Ensure proper initialization
            v.ensureProperInitialization();
            System.out.println("DEBUG - Found valid voucher: " + v.getCode() + ", Discount: " + v.getDiscountPercent() + "%, UsageCount: " + v.getUsageCount());
        } else {
            System.out.println("DEBUG - No valid voucher found for code: " + code);
            // Try to find any voucher with this code to see why it's invalid
            Optional<Voucher> anyVoucher = voucherRepository.findByCode(code);
            if (anyVoucher.isPresent()) {
                Voucher v = anyVoucher.get();
                v.ensureProperInitialization();
                System.out.println("DEBUG - Found voucher but it's invalid: " + v.getCode() 
                    + ", Active: " + v.getIsActive() 
                    + ", Expiration: " + v.getExpirationDate() 
                    + ", Current time: " + LocalDateTime.now()
                    + ", UsageCount: " + v.getUsageCount());
            } else {
                System.out.println("DEBUG - No voucher exists with code: " + code);
            }
        }
        return voucher;
    }

    @Override
    public Voucher getVoucherById(String id) {
        Voucher voucher = voucherRepository.findById(id).orElse(null);
        if (voucher != null) {
            voucher.ensureProperInitialization();
        }
        return voucher;
    }

    @Override
    public List<Voucher> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();
        // Ensure all vouchers are properly initialized
        for (Voucher voucher : vouchers) {
            voucher.ensureProperInitialization();
        }
        return vouchers;
    }

    @Override
    public Voucher updateVoucher(Voucher voucher) {
        // Ensure proper initialization before updating
        voucher.ensureProperInitialization();
        System.out.println("DEBUG - Saving voucher with usageCount: " + voucher.getUsageCount());
        Voucher savedVoucher = voucherRepository.save(voucher);
        System.out.println("DEBUG - Saved voucher with usageCount: " + savedVoucher.getUsageCount());
        return savedVoucher;
    }


    @Override
    public Long calculateDiscountAmount(Voucher voucher, Long originalAmount) {
        if (voucher == null) {
            System.out.println("DEBUG - calculateDiscountAmount: voucher is null");
            return 0L;
        }
        
        if (!voucher.getIsActive()) {
            System.out.println("DEBUG - calculateDiscountAmount: voucher is inactive");
            return 0L;
        }
        
        if (voucher.getExpirationDate().isBefore(LocalDateTime.now())) {
            System.out.println("DEBUG - calculateDiscountAmount: voucher is expired. Expiration: " 
                + voucher.getExpirationDate() + ", Current time: " + LocalDateTime.now());
            return 0L;
        }

        // Calculate discount based on percentage
        Long discountAmount = originalAmount * voucher.getDiscountPercent() / 100;
        System.out.println("DEBUG - calculateDiscountAmount: Original amount: " + originalAmount 
            + ", Discount percent: " + voucher.getDiscountPercent() 
            + "%, Initial discount amount: " + discountAmount);

        // Check if discount exceeds max discount amount
        if (voucher.getMaxDiscountAmount() != null && discountAmount > voucher.getMaxDiscountAmount()) {
            System.out.println("DEBUG - calculateDiscountAmount: Discount exceeds max amount. Capping at: " + voucher.getMaxDiscountAmount());
            discountAmount = voucher.getMaxDiscountAmount().longValue();
        }

        System.out.println("DEBUG - calculateDiscountAmount: Final discount amount: " + discountAmount);
        return discountAmount;
    }
    public Boolean toggleStatus(String voucherID) {
        try {
            Voucher voucher = this.voucherRepository.findById(voucherID).orElse(null);
            if (voucher != null) {
                voucher.setIsActive(!voucher.getIsActive());
                this.voucherRepository.save(voucher);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("error", e.getMessage(), e);
        }
        return false;
    }
} 