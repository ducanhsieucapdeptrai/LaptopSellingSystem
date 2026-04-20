package com.example.computershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.computershop.entity.Categories;

public interface CategoriesRepository extends JpaRepository<Categories, String> {
    boolean existsByName(String name);
}
