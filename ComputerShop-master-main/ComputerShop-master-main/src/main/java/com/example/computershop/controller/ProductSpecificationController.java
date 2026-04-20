package com.example.computershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.computershop.entity.ProductSpecifications;
import com.example.computershop.entity.Products;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.ProductSpecificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@AllArgsConstructor
@RequestMapping("/admin")
@Slf4j
public class ProductSpecificationController {
    private final ProductSpecificationService specificationService;
    private final ProductService productService;

    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String SPECIFICATION = "specification";
    private static final String PRODUCT = "product";
    private static final String SPECIFICATION_VIEW = "admin/product/specification";
    private static final String REDIRECT_PRODUCT = "redirect:/admin/product";

    private static final String SPEC_PATTERN = "^[\\p{L}\\p{N}.,()\\-\"\\s]*$";


    @GetMapping("/product/{productId}/specification")
    public String editSpecification(@PathVariable String productId, Model model) {
        Products product = productService.findById(productId);
        ProductSpecifications specification = specificationService.findByProductId(productId);
        if (specification == null) {
            specification = new ProductSpecifications();
            specification.setProduct(product);
        }
        model.addAttribute(PRODUCT, product);
        model.addAttribute(SPECIFICATION, specification);
        return SPECIFICATION_VIEW;
    }

    @PostMapping("/product/{productId}/specification")
    public String saveSpecification(@PathVariable String productId,
                                  @ModelAttribute ProductSpecifications specification,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            log.debug("Saving specification for product ID: {}", productId);
            Products product = productService.findById(productId);
            
            if (product == null) {
                log.error("Product not found with ID: {}", productId);
                redirectAttributes.addFlashAttribute(ERROR, "Không tìm thấy sản phẩm!");
                return REDIRECT_PRODUCT;
            }
            
            // Validate CPU
            if (specification.getCpu() != null && !specification.getCpu().trim().isEmpty() 
                && !specification.getCpu().matches(SPEC_PATTERN)) {
                model.addAttribute(ERROR, "CPU chỉ được chứa chữ, số, dấu '.', ',', '()', '-' , ngoặc kép và dấu cách.");
                model.addAttribute(PRODUCT, product);
                model.addAttribute(SPECIFICATION, specification);
                return SPECIFICATION_VIEW;
            }
            
            // Validate RAM
            if (specification.getRam() != null && !specification.getRam().trim().isEmpty() 
                && !specification.getRam().matches(SPEC_PATTERN)) {
                model.addAttribute(ERROR, "RAM chỉ được chứa chữ, số, dấu '.', ',', '()', '-', ngoặc kép và dấu cách.");
                model.addAttribute(PRODUCT, product);
                model.addAttribute(SPECIFICATION, specification);
                return SPECIFICATION_VIEW;
            }
            
            // Validate SSD
            if (specification.getSsd() != null && !specification.getSsd().trim().isEmpty() 
                && !specification.getSsd().matches(SPEC_PATTERN)) {
                model.addAttribute(ERROR, "SSD chỉ được chứa chữ, số, dấu '.', ',', '()', '-', ngoặc kép và dấu cách.");
                model.addAttribute(PRODUCT, product);
                model.addAttribute(SPECIFICATION, specification);
                return SPECIFICATION_VIEW;
            }
            
            // Validate VGA
            if (specification.getVga() != null && !specification.getVga().trim().isEmpty() 
                && !specification.getVga().matches(SPEC_PATTERN)) {
                model.addAttribute(ERROR, "VGA chỉ được chứa chữ, số, dấu '.', ',', '()', '-', ngoặc kép và dấu cách.");
                model.addAttribute(PRODUCT, product);
                model.addAttribute(SPECIFICATION, specification);
                return SPECIFICATION_VIEW;
            }
            
            // Validate Cổng kết nối
            if (specification.getConnect() != null && !specification.getConnect().trim().isEmpty() 
                && !specification.getConnect().matches(SPEC_PATTERN)) {
                model.addAttribute(ERROR, "Cổng kết nối chỉ được chứa chữ, số, dấu '.', ',', '()', '-', ngoặc kép và dấu cách.");
                model.addAttribute(PRODUCT, product);
                model.addAttribute(SPECIFICATION, specification);
                return SPECIFICATION_VIEW;
            }
            
            // Validate Pin
            if (specification.getBattery() != null && !specification.getBattery().trim().isEmpty() 
                && !specification.getBattery().matches(SPEC_PATTERN)) {
                model.addAttribute(ERROR, "Pin chỉ được chứa chữ, số, dấu '.', ',', '()', '-', ngoặc kép và dấu cách.");
                model.addAttribute(PRODUCT, product);
                model.addAttribute(SPECIFICATION, specification);
                return SPECIFICATION_VIEW;
            }
            
            // Validate Screen
            if (specification.getScreen() != null && !specification.getScreen().trim().isEmpty()
            && !specification.getScreen().matches(SPEC_PATTERN)) {
                model.addAttribute(ERROR, "Màn hình chỉ được chứa chữ, số, dấu '.', ',', '()', '-', ngoặc kép và dấu cách.");
                model.addAttribute(PRODUCT, product);
                model.addAttribute(SPECIFICATION, specification);
                return SPECIFICATION_VIEW;
            }
            
            // Set both product and productID for proper JPA mapping
            specification.setProduct(product);
            specification.setProductID(productId);
            
            specificationService.save(specification);
            log.info("Successfully saved specification for product: {} (ID: {})", product.getName(), productId);
            
            redirectAttributes.addFlashAttribute(SUCCESS, "Cấu hình sản phẩm đã được cập nhật thành công!");
        } catch (Exception e) {
            log.error("Error saving specification for product ID: {}", productId, e);
            redirectAttributes.addFlashAttribute(ERROR, "Có lỗi xảy ra khi cập nhật cấu hình sản phẩm: " + e.getMessage());
        }
        return REDIRECT_PRODUCT;
    }
}
