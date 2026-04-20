package com.example.computershop.service;

import com.example.computershop.entity.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    Voucher createVoucher(Voucher voucher);
    Optional<Voucher> getVoucherByCode(String code);
    Optional<Voucher> getValidVoucherByCode(String code);
    Voucher getVoucherById(String id);
    List<Voucher> getAllVouchers();
    Voucher updateVoucher(Voucher voucher);
    Long calculateDiscountAmount(Voucher voucher, Long originalAmount);
    Boolean toggleStatus(String voucherID);
} 