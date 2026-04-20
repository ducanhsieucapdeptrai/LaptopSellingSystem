package com.example.computershop.controller;

import com.example.computershop.config.RecaptchaConfig;
import com.example.computershop.dto.request.UserCreationRequest;
import com.example.computershop.dto.request.VerifyUserRequest;
import com.example.computershop.exception.AuthenticationException;
import com.example.computershop.service.AuthenticationService;
import com.example.computershop.service.RecaptchaService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    RecaptchaService recaptchaService;
    RecaptchaConfig recaptchaConfig;
    static String register = "auth/register";
    static String login = "auth/login";
    static String resendVerification = "auth/resendVerification";
    static String manualVerify = "auth/manual-verify";
    static String errorAttr = "error";
    static String messageAttr = "message";
    static String redirectlogin = "redirect:/auth/login";
    static String verifyAttr = "verifyRequest";
    static String forgotPassword = "auth/forgot-password";
    static String resetPassword = "auth/reset-password";
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserCreationRequest());
        return register;
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserCreationRequest request, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute(errorAttr, "Please check your registration information.");
            return register;
        }
        try {
            authenticationService.createUser(request);
            redirectAttributes.addFlashAttribute(messageAttr, "Registration successful! Please check your email to verify your account.");
            return redirectlogin;
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return register;
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute(errorAttr, error);
        }
        return login;
    }

    @GetMapping("/verify")
    public String verifyUserByLink(@RequestParam("email") String email, @RequestParam("code") String code, Model model, RedirectAttributes redirectAttributes) {
        VerifyUserRequest verifyRequest = new VerifyUserRequest();
        verifyRequest.setEmail(email);
        verifyRequest.setVerificationCode(code);
        
        try {
            if (authenticationService.isUserActive(email)) {
                redirectAttributes.addFlashAttribute(messageAttr, "Your account is already activated. You can log in now.");
                return redirectlogin;
            }

            authenticationService.verifyUser(verifyRequest);
            redirectAttributes.addFlashAttribute(messageAttr, "Account verified successfully. You can now log in.");
            return "redirect:/auth/login?verified=true";
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return login;
        }
    }

    @GetMapping("/resend-verification")
    public String showResendVerificationForm(Model model) {
        if (!model.containsAttribute(verifyAttr)) {
            model.addAttribute(verifyAttr, new VerifyUserRequest());
        }
        model.addAttribute("siteKey", recaptchaConfig.getSiteKey());
        return resendVerification;
    }

    @PostMapping("/resend-verification")
    public String resendVerificationEmail(
            @Valid @ModelAttribute("verifyRequest") VerifyUserRequest request,
            @RequestParam("g-recaptcha-response") String captchaResponse,
            BindingResult result,
            Model model) {
        
        // Verify reCAPTCHA
        if (!recaptchaService.isValidCaptcha(captchaResponse)) {
            model.addAttribute(errorAttr, "Please verify reCAPTCHA.");
            model.addAttribute("siteKey", recaptchaConfig.getSiteKey());
            return resendVerification;
        }
        
        if (result.hasErrors()) {
            model.addAttribute(errorAttr, "Please check your information.");
            model.addAttribute("siteKey", recaptchaConfig.getSiteKey());
            return resendVerification;
        }
        try {
            if (authenticationService.isUserActive(request.getEmail())) {
                model.addAttribute(messageAttr, "Your account is already activated. You can log in now.");
                return login;
            }
            authenticationService.resendVerificationEmail(request.getEmail());
            model.addAttribute(messageAttr, "Verification email has been resent. Please check your email.");
            return login;
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            model.addAttribute("siteKey", recaptchaConfig.getSiteKey());
            return resendVerification;
        }
    }

    @GetMapping("/manual-verify")
    public String showManualVerifyForm(Model model) {
        model.addAttribute(verifyAttr, new VerifyUserRequest());
        return manualVerify;
    }

    @PostMapping("/manual-verify")
    public String verifyUserManually(@Valid @ModelAttribute("verifyRequest") VerifyUserRequest request,
                                     BindingResult result,
                                     Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute(errorAttr, "Please check your verification information.");
            return manualVerify;
        }

        try {
            if (authenticationService.isUserActive(request.getEmail())) {
                redirectAttributes.addFlashAttribute(messageAttr, "Your account is already activated. You can log in now.");
                return redirectlogin;
            }
            authenticationService.verifyUser(request);
            redirectAttributes.addFlashAttribute(messageAttr, "Account verified successfully. You can now log in.");
            return "redirect:/auth/login?verified=true";
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            return manualVerify;
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("siteKey", recaptchaConfig.getSiteKey());
        return forgotPassword;
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String username, 
                                       @RequestParam("g-recaptcha-response") String captchaResponse,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        if (!recaptchaService.isValidCaptcha(captchaResponse)) {
            model.addAttribute(errorAttr, "Please verify reCAPTCHA.");
            model.addAttribute("siteKey", recaptchaConfig.getSiteKey());
            return forgotPassword;
        }
        
        try {
            authenticationService.sendPasswordResetEmail(username);
            redirectAttributes.addFlashAttribute(messageAttr, "Password reset instructions have been sent. Please check your email.");
            return redirectlogin;
        } catch (AuthenticationException e) {
            model.addAttribute(errorAttr, e.getMessage());
            model.addAttribute("siteKey", recaptchaConfig.getSiteKey());
            return forgotPassword;
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String email, @RequestParam String token, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (!authenticationService.validatePasswordResetToken(email, token)) {
                redirectAttributes.addFlashAttribute(errorAttr, "The password reset link is invalid or has expired.");
                return redirectlogin;
            }
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return resetPassword;
        } catch (AuthenticationException e) {
            redirectAttributes.addFlashAttribute(errorAttr, e.getMessage());
            return redirectlogin;
        }
    }

    private String validatePassword(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least 1 uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least 1 lowercase letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least 1 number";
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return "Password must contain at least 1 special character";
        }
        return null;
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email,
                                       @RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String passwordConfirm,
                                       Model model, RedirectAttributes redirectAttributes) {
        try {   
            if (!password.equals(passwordConfirm)) {
                model.addAttribute(errorAttr, "Confirm password does not match!");
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return resetPassword;
            }

            String passwordError = validatePassword(password);
            if (passwordError != null) {
                model.addAttribute(errorAttr, passwordError);
                model.addAttribute("email", email);
                model.addAttribute("token", token);
                return resetPassword;
            }

            authenticationService.resetPassword(email, token, password);
            redirectAttributes.addFlashAttribute(messageAttr, "Password reset successful. You can now log in with your new password.");
            return redirectlogin;
        } catch (AuthenticationException e) {
            redirectAttributes.addFlashAttribute(errorAttr, e.getMessage());
            return resetPassword;
        }
    }
}
