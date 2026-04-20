package com.example.computershop.enums;

import lombok.Getter;

@Getter
public enum Role {
    Admin("Quản trị viên"),
    ADMIN("Quản trị viên"), // Backward compatibility
    Customer("Người dùng"),
    CUSTOMER("Người dùng"), // For uppercase conversion
    Shipper("Nhân viên vận chuyển"),
    SHIPPER("Nhân viên vận chuyển"), // For uppercase conversion
    Sales("Nhân viên bán hàng"),
    SALES("Nhân viên bán hàng"); // For uppercase conversion

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

} 