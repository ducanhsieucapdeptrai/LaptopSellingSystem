package com.example.computershop.controller;

import com.example.computershop.entity.Categories;
import com.example.computershop.service.CategoriesService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class CategoriesController {
    private CategoriesService service;
    private static final String ERROR = "error";
    private static final String CATEGORIES = "categories";
    private static final String CATEGORIES_VIEW = "admin/categories/categories";
    private static final String CATEGORIES2 = "redirect:/admin/categories";
    private static final String CATEGORIES_ADD = "admin/categories/add";
    private static final String CATEGORIES_EDIT = "admin/categories/edit";

    @GetMapping("/categories")
    public String showCategoriesPage(Model model){
        List<Categories> list = this.service.getAll();
        model.addAttribute(CATEGORIES, list);
        return CATEGORIES_VIEW;
    }

    @RequestMapping("/add-categories")
    public String add(Model model) {
        Categories categories = new Categories();
        model.addAttribute(CATEGORIES, categories);
        return CATEGORIES_ADD;
    }

    @PostMapping("/add-categories")
    public String addCategories(@ModelAttribute("categories") Categories categories, Model model) {
        if (!categories.getName().matches("^[\\p{L}\\s]+$")) {
            model.addAttribute(ERROR, "Tên danh mục không hợp lệ.");
            model.addAttribute(CATEGORIES, categories);
            return CATEGORIES_EDIT;
        }
        if (service.existsByName(categories.getName())) {
            model.addAttribute(ERROR, "Danh mục đã tồn tại.");
            return CATEGORIES_EDIT;
        }
        if (Boolean.TRUE.equals(this.service.create(categories))) {
            return CATEGORIES2;
        } else {
            return CATEGORIES_ADD;
        }
    }

    @GetMapping("/edit-categories/{categoryID}")
    public String editCategories(Model model, @PathVariable("categoryID") String categoryID) {
        Categories categories = this.service.findById(categoryID);
        model.addAttribute(CATEGORIES, categories);
        return CATEGORIES_EDIT;
    }

    @PostMapping("/edit-categories")
    public String updateCategories(@ModelAttribute("categories") Categories categories, Model model) {
        Categories existingCategory = this.service.findById(categories.getCategoryID());
        if (!categories.getName().matches("^[\\p{L}\\s]+$")) {
            model.addAttribute(ERROR, "Tên danh mục không hợp lệ.");
            model.addAttribute(CATEGORIES, categories);
            return CATEGORIES_EDIT;
        }
        if (!existingCategory.getName().equals(categories.getName()) && 
            service.existsByName(categories.getName())) {
            model.addAttribute(ERROR, "Danh mục đã tồn tại.");
            model.addAttribute(CATEGORIES, categories);
            return CATEGORIES_EDIT;
        }

        if (Boolean.TRUE.equals(this.service.create(categories))) {
            return CATEGORIES2;
        } else {
            return CATEGORIES_EDIT;
        }
    }

    @PostMapping("/toggle-category-status/{categoryID}")
    @ResponseBody
    public String toggleCategoryStatus(@PathVariable("categoryID") String categoryID) {
        try {
            if (Boolean.TRUE.equals(this.service.toggleStatus(categoryID))) {
                return "success";
            }
            return "error";
        } catch (Exception e) {
            return "error";
        }
    }
}
