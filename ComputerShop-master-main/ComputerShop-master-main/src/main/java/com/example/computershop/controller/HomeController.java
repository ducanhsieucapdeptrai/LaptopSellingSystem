package com.example.computershop.controller;

import com.example.computershop.entity.Categories;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.User;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.service.CategoriesService;
import com.example.computershop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoriesService categoriesService;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public HomeController(ProductService productService, CategoriesService categoriesService,
                         CartRepository cartRepository, UserRepository userRepository) {
        this.productService = productService;
        this.categoriesService = categoriesService;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    /**
     * Homepage with dynamic product data
     */
    @GetMapping({"/", "/home", "/user/homepage"})
    public String homepage(Model model, Principal principal) {
        try {
            // Get all active products - optimized query without specifications for homepage
            List<Products> allProducts = productService.getAllActiveForHomepage();

            // Get featured/popular products (limit to 8 for homepage)
            List<Products> featuredProducts = allProducts.stream()
                .limit(8)
                .collect(Collectors.toList());

            // Get products for main slider (first 4 products)
            List<Products> sliderProducts = allProducts.stream()
                .limit(4)
                .collect(Collectors.toList());

            // Get new products (sorted by creation date, newest first, limit to 6)
            List<Products> newProducts = allProducts.stream()
                .sorted((p1, p2) -> {
                    if (p1.getCreatedAt() == null && p2.getCreatedAt() == null) return 0;
                    if (p1.getCreatedAt() == null) return 1;
                    if (p2.getCreatedAt() == null) return -1;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt()); // Newest first
                })
                .limit(6)
                .collect(Collectors.toList());

            // Get most sold/hot products (simulate popularity based on low stock = high demand)
            List<Products> mostSoldProducts = allProducts.stream()
                .filter(product -> product.getQuantity() != null)
                .sorted((p1, p2) -> {
                    // Sort by quantity ascending (lower stock = more popular)
                    int quantityCompare = Integer.compare(p1.getQuantity(), p2.getQuantity());
                    if (quantityCompare != 0) return quantityCompare;
                    // Secondary sort by price descending (higher price products are often premium/popular)
                    return p2.getPrice().compareTo(p1.getPrice());
                })
                .limit(8)
                .collect(Collectors.toList());

            // Get categories for Browse by Category section
            List<Categories> categories = categoriesService.getAll();
            
            // Get brands for Browse by Brand section
            List<String> brands = productService.getDistinctBrands();
            
            // If no brands in database, add some popular computer brands for display
            if (brands.isEmpty()) {
                brands = java.util.Arrays.asList(
                    "Apple", "Dell", "HP", "Lenovo", "Asus", 
                    "MSI", "Acer", "ThinkPad", "Razer", "Alienware"
                );
            }

            // Get cart count for header (0 if not logged in)
            int cartCount = getCartCount(principal);

            // Add data to model
            model.addAttribute("featuredProducts", featuredProducts);
            model.addAttribute("sliderProducts", sliderProducts);
            model.addAttribute("newProducts", newProducts);
            model.addAttribute("mostSoldProducts", mostSoldProducts);
            model.addAttribute("categories", categories);
            model.addAttribute("brands", brands);
            model.addAttribute("cartCount", cartCount);
            model.addAttribute("totalProducts", allProducts.size());
            model.addAttribute("isAuthenticated", principal != null);

            return "user/homepage";
        } catch (Exception e) {
            // Log error and return homepage with empty data
            List<String> fallbackBrands = java.util.Arrays.asList(
                "Apple", "Dell", "HP", "Lenovo", "Asus", 
                "MSI", "Acer", "ThinkPad", "Razer", "Alienware"
            );
            model.addAttribute("featuredProducts", java.util.Collections.emptyList());
            model.addAttribute("sliderProducts", java.util.Collections.emptyList());
            model.addAttribute("newProducts", java.util.Collections.emptyList());
            model.addAttribute("mostSoldProducts", java.util.Collections.emptyList());
            model.addAttribute("categories", java.util.Collections.emptyList());
            model.addAttribute("brands", fallbackBrands);
            model.addAttribute("cartCount", 0);
            model.addAttribute("totalProducts", 0);
            return "user/homepage";
        }
    }

    /**
     * Get cart count for current user
     */
    private int getCartCount(Principal principal) {
        if (principal == null) {
            return 0;
        }
        
        try {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                return cartRepository.findByUser(user).stream()
                    .mapToInt(cart -> cart.getQuantity())
                    .sum();
            }
        } catch (Exception e) {
            // Return 0 if error occurs
        }
        
        return 0;
    }
} 