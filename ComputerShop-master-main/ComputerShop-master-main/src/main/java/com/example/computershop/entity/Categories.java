package com.example.computershop.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String categoryID;

    @Column(name = "name",nullable = false, columnDefinition = "nvarchar(255)")
    String name;
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    String description;
    
    @Column(name = "is_active", columnDefinition = "bit")
    Boolean isActive = true;
    
    @OneToMany(mappedBy = "categories")
    @JsonIgnore
    Set<Products> products;

}
