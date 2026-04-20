package com.example.computershop.controller;

import com.example.computershop.entity.Voucher;
import com.example.computershop.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    @Autowired
    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public String listVouchers(Model model) {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        model.addAttribute("vouchers", vouchers);
        return "admin/vouchers/list";
    }

    @GetMapping("/add")
    public String showAddVoucherForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "admin/vouchers/add";
    }

    @PostMapping("/add")
    public String addVoucher(
            @RequestParam("code") String code,
            @RequestParam("discountPercent") Integer discountPercent,
            @RequestParam("maxDiscountAmount") Integer maxDiscountAmount,
            @RequestParam("expirationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expirationDate,
            @RequestParam(value = "isActive", defaultValue = "true") Boolean isActive,
            RedirectAttributes redirectAttributes) {

        // Validate voucher code uniqueness
        Optional<Voucher> existingVoucher = voucherService.getVoucherByCode(code);
        if (existingVoucher.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Voucher code already exists");
            return "redirect:/admin/vouchers/add";
        }

        // Validate discount percent
        if (discountPercent <= 0 || discountPercent > 100) {
            redirectAttributes.addFlashAttribute("error", "Discount percent must be between 1 and 100");
            return "redirect:/admin/vouchers/add";
        }

        // Create and save voucher
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDiscountPercent(discountPercent);
        voucher.setMaxDiscountAmount(maxDiscountAmount);
        voucher.setExpirationDate(expirationDate);
        voucher.setIsActive(isActive);

        voucherService.createVoucher(voucher);
        redirectAttributes.addFlashAttribute("success", "Voucher created successfully");
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String showEditVoucherForm(@PathVariable("id") String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Voucher> voucherOpt = Optional.ofNullable(voucherService.getVoucherById(id));
        
        if (voucherOpt.isPresent()) {
            model.addAttribute("voucher", voucherOpt.get());
            return "admin/vouchers/edit";
        } else {
            redirectAttributes.addFlashAttribute("error", "Voucher not found");
            return "redirect:/admin/vouchers";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateVoucher(
            @PathVariable("id") String id,
            @RequestParam("code") String code,
            @RequestParam("discountPercent") Integer discountPercent,
            @RequestParam("maxDiscountAmount") Integer maxDiscountAmount,
            @RequestParam("expirationDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expirationDate,
            @RequestParam(value = "isActive", defaultValue = "false") Boolean isActive,
            RedirectAttributes redirectAttributes) {

        Optional<Voucher> voucherOpt = Optional.ofNullable(voucherService.getVoucherById(id));
        
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            
            // Check if code is changed and already exists
            if (!voucher.getCode().equals(code)) {
                Optional<Voucher> existingVoucher = voucherService.getVoucherByCode(code);
                if (existingVoucher.isPresent()) {
                    redirectAttributes.addFlashAttribute("error", "Voucher code already exists");
                    return "redirect:/admin/vouchers/edit/" + id;
                }
            }
            
            // Validate discount percent
            if (discountPercent <= 0 || discountPercent > 100) {
                redirectAttributes.addFlashAttribute("error", "Discount percent must be between 1 and 100");
                return "redirect:/admin/vouchers/edit/" + id;
            }
            
            // Update voucher
            voucher.setCode(code);
            voucher.setDiscountPercent(discountPercent);
            voucher.setMaxDiscountAmount(maxDiscountAmount);
            voucher.setExpirationDate(expirationDate);
            voucher.setIsActive(isActive);
            
            voucherService.updateVoucher(voucher);
            redirectAttributes.addFlashAttribute("success", "Voucher updated successfully");
        } else {
            redirectAttributes.addFlashAttribute("error", "Voucher not found");
        }
        
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/toggle-voucher-status/{voucherID}")
    @ResponseBody
    public String toggleVoucherStatus(@PathVariable("voucherID") String voucherID) {
        try {
            if (Boolean.TRUE.equals(this.voucherService.toggleStatus(voucherID))) {
                return "success";
            }
            return "error";
        } catch (Exception e) {
            return "error";
        }
    }
} 