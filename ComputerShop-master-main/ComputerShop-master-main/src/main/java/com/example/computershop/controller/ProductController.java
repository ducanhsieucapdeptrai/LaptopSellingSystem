package com.example.computershop.controller;

import com.example.computershop.entity.Categories;
import com.example.computershop.entity.Products;
import com.example.computershop.service.CategoriesService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.List;


@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class ProductController {

    private ProductService productService;
    private CategoriesService categoriesService;
    private StorageService storageService;
    private static final String PRODUCT = "product";
    private static final String CATEGORIES = "categories";
    private static final String ERROR = "error";

    private static final String PRODUCT_VIEW = "admin/product/product";
    private static final String PRODUCT_VIEW2 = "redirect:/admin/product";
    private static final String PRODUCT_ADD = "admin/product/add";
    private static final String PRODUCT_EDIT =  "admin/product/edit";

    private String handleInvalidProduct(Products product, MultipartFile file, Model model, 
                                      String errorMessage, Products existingProduct, boolean isEdit) {
        model.addAttribute(ERROR, errorMessage);
        List<Categories> categories = this.categoriesService.getAll();
        model.addAttribute(CATEGORIES, categories);
        model.addAttribute(PRODUCT, product);

        if (isEdit) {
            if (file.isEmpty()) {
                product.setImageURL(existingProduct.getImageURL());
            } else {
                String fileName = file.getOriginalFilename();
                product.setImageURL(fileName);
            }
            return PRODUCT_EDIT;
        } else {
            if (!file.isEmpty()) {
                String fileName = file.getOriginalFilename();
                product.setImageURL(fileName);
            }
            return PRODUCT_ADD;
        }
    }

    @RequestMapping("/product")
    public String index(Model model) {
        List<Products> list = this.productService.getAll();
        model.addAttribute(PRODUCT, list);
        return PRODUCT_VIEW;
    }

    @RequestMapping("/add-product")
    public String add(Model model) {
        Products products = new Products();
        model.addAttribute(PRODUCT, products);
        List<Categories> list = this.categoriesService.getAll();
        model.addAttribute(CATEGORIES, list);
        return PRODUCT_ADD;
    }

    @GetMapping("/product")
    public String showProducts(Model model) {
        List<Products> list = this.productService.getAll();
        model.addAttribute(PRODUCT, list);
        return PRODUCT_VIEW;
    }

    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute("product") Products product, 
                           @RequestParam("productImage") MultipartFile file,
                           Model model) {
        if (!product.getBrand().matches("^[a-zA-Z\\s]+$")||product.getPrice().compareTo(BigInteger.ZERO) <= 0|| product.getQuantity() < 0 ||
                !product.getName().matches("^[\\-\\p{L}\\p{N}\\s]+$")) {
            return handleInvalidProduct(product, file, model, "Thông tin sản phẩm không hợp lệ.", null, false);
        }
        if (productService.existsByName(product.getName())) {
            return handleInvalidProduct(product, file, model, "Sản phẩm đã tồn tại.", null, false);
        }
        this.storageService.store(file);
        String fileName = file.getOriginalFilename();
        product.setImageURL(fileName);
        if (Boolean.TRUE.equals(this.productService.create(product))) {
            return PRODUCT_VIEW2;
        }
        return PRODUCT_ADD;
    }

    @GetMapping("/edit-product/{productID}")
    public String editProduct(Model model, @PathVariable("productID") String productID) {
        Products products = this.productService.findById(productID);
        List<Categories> list = this.categoriesService.getAll();
        model.addAttribute(CATEGORIES, list);
        model.addAttribute(PRODUCT, products);
        return PRODUCT_EDIT;
    }

    @PostMapping("/edit-product")
    public String updateProduct(@ModelAttribute("product") Products product, 
                              @RequestParam("productImage") MultipartFile file,
                              Model model) {
        Products existingProduct = this.productService.findById(product.getProductID());
        if (!product.getBrand().matches("^[a-zA-Z\\s]+$")||product.getPrice().compareTo(BigInteger.ZERO) <= 0|| product.getQuantity() < 0 ||
               !product.getName().matches("^[\\-\\p{L}\\p{N}\\s]+$")) {
            return handleInvalidProduct(product, file, model, "Thông tin sản phẩm không hợp lệ.", existingProduct, true);
        }
        if (!existingProduct.getName().equals(product.getName()) && 
            productService.existsByName(product.getName())) {
            return handleInvalidProduct(product, file, model, "Sản phẩm đã tồn tại.", existingProduct, true);
        }

        String fileName;
        if (file.isEmpty()) {
            fileName = existingProduct.getImageURL();
            product.setImageURL(fileName);
        } else {
            this.storageService.store(file);
            fileName = file.getOriginalFilename();
            product.setImageURL(fileName);
        }

        if (Boolean.TRUE.equals(this.productService.create(product))) {
            return PRODUCT_VIEW2;
        } else {
            return PRODUCT_EDIT;
        }
    }

    @PostMapping("/toggle-product-status/{productID}")
    @ResponseBody
    public String toggleProductStatus(@PathVariable("productID") String productID) {
        try {
            if (Boolean.TRUE.equals(this.productService.toggleStatus(productID))) {
                return "success";
            }
            return "error";
        } catch (Exception e) {
            return "error";
        }
    }
}
