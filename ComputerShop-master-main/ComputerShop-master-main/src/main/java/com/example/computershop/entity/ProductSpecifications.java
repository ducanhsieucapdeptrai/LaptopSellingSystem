package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "Product_Specifications")
public class ProductSpecifications {
    @Id
    @Column(name = "productID", columnDefinition = "UNIQUEIDENTIFIER")
    String productID;
    
    @Column(name = "cpu", columnDefinition = "NVARCHAR(255)")
    String cpu;
    
    @Column(name = "ram", columnDefinition = "NVARCHAR(255)")
    String ram;
    
    @Column(name = "ssd", columnDefinition = "NVARCHAR(255)")
    String ssd;
    
    @Column(name = "vga", columnDefinition = "NVARCHAR(255)")
    String vga;
    
    @Column(name = "screen", columnDefinition = "NVARCHAR(255)")
    String screen;
    
    @Column(name = "weight")
    Float weight;
    
    @Column(name = "connect", columnDefinition = "NVARCHAR(255)")
    String connect;
    
    @Column(name = "battery", columnDefinition = "NVARCHAR(255)")
    String battery;
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "productID", referencedColumnName = "productID", columnDefinition = "UNIQUEIDENTIFIER")
    private Products product;
}
