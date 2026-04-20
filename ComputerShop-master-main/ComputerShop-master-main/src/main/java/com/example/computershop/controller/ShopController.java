package com.example.computershop.controller;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.computershop.dto.ProductRatingDTO;
import com.example.computershop.entity.Categories;
import com.example.computershop.entity.ProductSpecifications;
import com.example.computershop.entity.ProductVariant;
import com.example.computershop.entity.Products;
import com.example.computershop.service.CategoriesService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.ProductSpecificationService;
import com.example.computershop.service.ProductVariantService;
import com.example.computershop.service.ReviewService;

@Controller
@RequestMapping("/user")
public class ShopController {
    
    private final CategoriesService categoriesService;
    private final ProductService productService;
    private final ProductSpecificationService productSpecificationService;
    private final ProductVariantService productVariantService;
    private final ReviewService reviewService;
    private static final String TOTAL_PRODUCTS = "totalProducts";
    private static final String PRODUCTS = "products";
    private static final String CATEGORIES = "categories";

    public ShopController(CategoriesService categoriesService,
                         ProductService productService,
                         ProductSpecificationService productSpecificationService,
                         ProductVariantService productVariantService,
                         ReviewService reviewService) {
        this.categoriesService = categoriesService;
        this.productService = productService;
        this.productSpecificationService = productSpecificationService;
        this.productVariantService = productVariantService;
        this.reviewService = reviewService;
    }

    @GetMapping("/shopping-page")
    public String showShoppingPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String cpu,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String ssd,
            @RequestParam(required = false) String vga,
            @RequestParam(required = false) String screen,
            @RequestParam(required = false) Long priceMin,
            @RequestParam(required = false) Long priceMax,
            Model model) {
        
        List<Categories> categories = categoriesService.getAll();
        model.addAttribute(CATEGORIES, categories);
        
        List<String> brands = productService.getDistinctBrands();
        model.addAttribute("brands", brands);

        List<Products> filteredProducts = filterProducts(search, category, brand, cpu, ram, ssd, vga, screen, priceMin, priceMax);
        
        model.addAttribute(PRODUCTS, filteredProducts);
        model.addAttribute(TOTAL_PRODUCTS, filteredProducts.size());

        model.addAttribute("searchTerm", search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedBrand", brand);
        model.addAttribute("selectedCpu", cpu);
        model.addAttribute("selectedRam", ram);
        model.addAttribute("selectedSsd", ssd);
        model.addAttribute("selectedVga", vga);
        model.addAttribute("selectedScreen", screen);
        model.addAttribute("selectedPriceMin", priceMin);
        model.addAttribute("selectedPriceMax", priceMax);
        
        return "user/shoppingpage";
    }
    
    private List<Products> filterProducts(String search, String category, String brand, String cpu, String ram, String ssd, String vga, String screen, Long priceMin, Long priceMax) {
        List<Products> allProducts;
        
        // Nếu có từ khóa tìm kiếm, sử dụng search function
        if (search != null && !search.trim().isEmpty()) {
            allProducts = productService.searchProductsByName(search);
        } else {
            allProducts = productService.getAllActiveWithSpecifications();
        }
        
        return allProducts.stream()
            .filter(product -> {
                try {
                    Boolean isActive = (Boolean) product.getClass().getMethod("getIsActive").invoke(product);
                    return isActive != null && isActive;
                } catch (Exception e) {
                    return true; // If can't check, include it
                }
            })
            .filter(product -> filterByCategory(product, category))
            .filter(product -> filterByBrand(product, brand))
            .filter(product -> filterBySpecification(product, cpu, "cpu"))
            .filter(product -> filterBySpecification(product, ram, "ram"))
            .filter(product -> filterBySpecification(product, ssd, "ssd"))
            .filter(product -> filterBySpecification(product, vga, "vga"))
            .filter(product -> filterBySpecification(product, screen, "screen"))
            .filter(product -> filterByPrice(product, priceMin, priceMax))
            .toList();
    }
    
    private boolean filterByCategory(Products product, String categoryFilter) {
        if (categoryFilter == null || categoryFilter.trim().isEmpty()) {
            return true;
        }
        
        try {
            Categories productCategory = (Categories) product.getClass().getMethod("getCategories").invoke(product);
            if (productCategory == null) {
                return false;
            }
            
            String categoryName = (String) productCategory.getClass().getMethod("getName").invoke(productCategory);
            if (categoryName == null) {
                return false;
            }
            
            List<String> categoryNames = Arrays.asList(categoryFilter.split(","));
            return categoryNames.stream()
                .anyMatch(name -> name.trim().equalsIgnoreCase(categoryName));
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean filterByBrand(Products product, String brandFilter) {
        if (brandFilter == null || brandFilter.trim().isEmpty()) {
            return true; // No filter applied
        }
        try {
            String productBrand = (String) product.getClass().getMethod("getBrand").invoke(product);
            if (productBrand == null) {
                return false;
            }
            
            List<String> brandNames = Arrays.asList(brandFilter.split(","));
            return brandNames.stream()
                .anyMatch(brand -> brand.trim().equalsIgnoreCase(productBrand));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean filterBySpecification(Products product, String specFilter, String specType) {
        if (specFilter == null || specFilter.trim().isEmpty()) {
            return true; // No filter applied
        }
        
        try {
            // Get the specifications for this product
            Object specs = product.getClass().getMethod("getSpecifications").invoke(product);
            if (specs == null) {
                return false;
            }
            
            // Get the specific specification value based on type
            String specValue;
            switch (specType) {
                case "cpu":
                    specValue = (String) specs.getClass().getMethod("getCpu").invoke(specs);
                    break;
                case "ram":
                    specValue = (String) specs.getClass().getMethod("getRam").invoke(specs);
                    break;
                case "ssd":
                    specValue = (String) specs.getClass().getMethod("getSsd").invoke(specs);
                    break;
                case "vga":
                    specValue = (String) specs.getClass().getMethod("getVga").invoke(specs);
                    break;
                case "screen":
                    specValue = (String) specs.getClass().getMethod("getScreen").invoke(specs);
                    break;
                default:
                    return false;
            }
            
            if (specValue == null) {
                return false;
            }
            
            // Check if the product specification matches any of the filter values
            List<String> filterValues = Arrays.asList(specFilter.split(","));
            final String finalSpecValue = specValue.toLowerCase();
            return filterValues.stream()
                .anyMatch(filterValue -> finalSpecValue.contains(filterValue.trim().toLowerCase()));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean filterByPrice(Products product, Long priceMin, Long priceMax) {
        if (priceMin == null && priceMax == null) {
            return true; // No price filter applied
        }
        
        try {
            Object priceObj = product.getClass().getMethod("getPrice").invoke(product);
            if (priceObj == null) {
                return false;
            }

            long productPrice = ((java.math.BigInteger) priceObj).longValue();

            return (priceMin == null || productPrice >= priceMin) && 
                   (priceMax == null || productPrice <= priceMax);
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/single-product")
    public String showSingleProduct(@RequestParam("id") String productId, Model model) {
        Products product = productService.findById(productId);
        if (product == null) {
            return "redirect:/user/shopping-page";
        }
        
        ProductSpecifications specifications = productSpecificationService.findByProductId(productId);
        List<ProductVariant> variants = productVariantService.getActiveVariantsByProductSorted(productId);
        
        // Thêm thông tin rating
        ProductRatingDTO rating = reviewService.getProductRating(productId);
        
        model.addAttribute("product", product);
        model.addAttribute("specifications", specifications);
        model.addAttribute("variants", variants);
        model.addAttribute("rating", rating); // Thêm rating vào model

        
        return "user/singleproduct";
    }

} 