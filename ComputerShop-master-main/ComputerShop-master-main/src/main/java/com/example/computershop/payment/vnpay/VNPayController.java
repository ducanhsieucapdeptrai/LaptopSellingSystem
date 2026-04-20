package com.example.computershop.payment.vnpay;

import com.example.computershop.entity.Order;
import com.example.computershop.entity.Payment;
import com.example.computershop.service.impl.OrderServiceImpl;
import com.example.computershop.service.IPaymentService;
import com.example.computershop.payment.vnpay.VNPayService.VNPayPaymentRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.Optional;

@Controller
@RequestMapping()
@Slf4j
public class VNPayController {
    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderServiceImpl orderService;
    
    @Autowired
    private IPaymentService paymentService;

    // ===== NEW FLOW: Database-driven checkout =====
    
    // Chuyển hướng người dùng đến cổng thanh toán VNPAY
    @GetMapping({"/user/checkout/vnpay"})
    public String submidOrder(HttpServletRequest request, @RequestParam("orderId") String orderId) {
        try {
            // Validate orderId
            if (orderId == null || orderId.trim().isEmpty()) {
                log.error("Invalid orderId for VNPay payment: {}", orderId);
                return "redirect:/cart/checkout?error=invalid_order_id";
            }
            
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            
            // Get order from database instead of session
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order == null) {
                log.error("Order not found for VNPay payment: {}", orderId);
                return "redirect:/cart/checkout?error=order_not_found";
            }
            
            // ✅ Verify order is in correct status for payment
            if (!"PAYMENT_PENDING".equals(order.getStatus())) {
                log.error("Order {} is not in PAYMENT_PENDING status. Current status: {}", orderId, order.getStatus());
                return "redirect:/cart/checkout?error=invalid_order_status";
            }
            
            // Validate userId
            String userId = order.getUserId();
            if (userId == null || userId.trim().isEmpty()) {
                log.error("Order {} has no associated userId", orderId);
                return "redirect:/cart/checkout?error=invalid_user_id";
            }
            
            long totalAmountVND = order.getTotalAmount();
            
            log.info("Processing VNPay payment for order {}: {} VND", orderId, totalAmountVND);
            
            // Generate unique transaction reference
            String vnpTxnRef = VNPayConfig.getRandomNumber(8);
            
            // Create VNPay payment URL with transaction reference
            VNPayPaymentRequest paymentRequest;
            try {
                paymentRequest = vnPayService.createOrder(request, totalAmountVND, baseUrl, vnpTxnRef);
            } catch (IllegalArgumentException e) {
                log.error("VNPay amount validation failed for order {}: {}", orderId, e.getMessage());
                return "redirect:/cart/checkout?error=amount_invalid&message=" + 
                       java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            }
            
            // Check if payment record already exists for this order
            try {
                Optional<Payment> existingPayment = paymentService.findByOrderId(UUID.fromString(orderId));
                
                if (existingPayment.isPresent()) {
                    // Update existing payment with new transaction reference
                    Payment payment = existingPayment.get();
                    payment.setOrderId(UUID.fromString(order.getId()));
                    payment.setVnpTxnRef(paymentRequest.getVnpTxnRef());
                    payment.setPaymentStatus("Pending");
                    payment.setPaidAmount(totalAmountVND);
                    payment.setUpdatedAt(LocalDateTime.now());
                    payment.setOrderInfo(order.getNote());
                    paymentService.save(payment);
                    
                    log.info("Updated existing VNPay payment record for order {}", orderId);
                } else {
                    // Create new payment record
                    paymentService.createVNPayPayment(
                        UUID.fromString(orderId), 
                        UUID.fromString(userId),
                        totalAmountVND, 
                        paymentRequest.getVnpTxnRef()
                    );
                    
                    log.info("Created new VNPay payment record for order {}", orderId);
                }
                
                return "redirect:" + paymentRequest.getPaymentUrl();
                
            } catch (Exception e) {
                log.error("Failed to create/update VNPay payment record", e);
                return "redirect:/cart/checkout?error=payment_creation_failed&message=" + 
                       java.net.URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            }
            
        } catch (Exception e) {
            log.error("Error in VNPay checkout", e);
            return "redirect:/cart/checkout?error=vnpay_error";
        }
    }

    // ===== OLD FLOW COMPATIBILITY: Redirect to checkout =====
    
    /**
     * Handle old flow URLs - redirect to checkout page
     * This handles direct access to /payment/vnpay/qr with form data
     */
    @GetMapping("/payment/vnpay/qr")
    public String handleOldQrFlow(HttpServletRequest request) {
        log.warn("Old VNPay QR flow accessed, redirecting to checkout. URL: {}", request.getRequestURL());
        
        // Extract some key parameters for debugging
        String fullName = request.getParameter("fullName");
        String paymentMethod = request.getParameter("paymentMethod");
        
        log.info("Old flow parameters - fullName: {}, paymentMethod: {}", fullName, paymentMethod);
        
        // Redirect to checkout page with message
        return "redirect:/cart/checkout?info=please_use_checkout_form";
    }

    // ===== RETURN FLOW =====

    // Sau khi hoàn tất thanh toán, VNPAY sẽ chuyển hướng trình duyệt về URL này
    @GetMapping("/vnpay-payment-return")
    public String paymentCompleted(HttpServletRequest request, Model model){
        try {
            int paymentStatus = vnPayService.orderReturn(request);

            String orderInfo = request.getParameter("vnp_OrderInfo");
            String paymentTime = request.getParameter("vnp_PayDate");
            String transactionId = request.getParameter("vnp_TransactionNo");
            String totalPrice = request.getParameter("vnp_Amount");
            String vnpTxnRef = request.getParameter("vnp_TxnRef");
            String vnpResponseCode = request.getParameter("vnp_ResponseCode");
            String vnpTransactionStatus = request.getParameter("vnp_TransactionStatus");
            String vnpBankCode = request.getParameter("vnp_BankCode");
            String vnpCardType = request.getParameter("vnp_CardType");

            log.info("VNPay return - TxnRef: {}, Status: {}, ResponseCode: {}", 
                    vnpTxnRef, paymentStatus, vnpResponseCode);

            // Format payment time
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(VNPayConstants.VNPAY_DATE_FORMAT);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(VNPayConstants.DISPLAY_DATE_FORMAT);
            
            String formattedDate = "";
            if (paymentTime != null && !paymentTime.isEmpty()) {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(paymentTime, inputFormatter);
                    formattedDate = dateTime.format(outputFormatter);
                } catch (Exception e) {
                    log.error("Error parsing payment time: " + paymentTime, e);
                    formattedDate = paymentTime;
                }
            }

            // Format currency
            String totalPriceVND = "";
            if (totalPrice != null && totalPrice.length() >= 2) {
                totalPriceVND = totalPrice.substring(0, totalPrice.length() - 2);
                NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                try {
                    totalPriceVND = formatter.format(Long.parseLong(totalPriceVND));
                } catch (NumberFormatException e) {
                    log.error("Error formatting total price: " + totalPrice, e);
                }
            }

            // Update payment record if VNPay transaction reference exists
            if (vnpTxnRef != null) {
                try {
                    Optional<Payment> paymentOpt = paymentService.findByVnpTxnRef(vnpTxnRef);
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentService.updateVNPayPaymentResult(
                            vnpTxnRef, vnpResponseCode, vnpTransactionStatus, 
                            paymentTime, transactionId, vnpBankCode, vnpCardType
                        );
                        log.info("Updated VNPay payment result for TxnRef: {}", vnpTxnRef);
                        
                        // ✅ NEW FLOW: Update Order status based on payment result
                        if (payment != null && payment.getOrderId() != null) {
                            Order order = orderService.getOrderById(payment.getOrderId().toString());
                            if (order != null) {
                                if (paymentStatus == 1) {
                                    String oldStatus = order.getStatus();
                                    // Payment successful - update order status to CONFIRMED
                                    order.setStatus("CONFIRMED");
                                    orderService.updateOrder(order, oldStatus);
                                    log.info("Updated order {} status to CONFIRMED after successful VNPay payment", order.getId());
                                } else if (paymentStatus == 0) {
                                    String oldStatus = order.getStatus();
                                    // Payment failed - update order status to CANCELLED
                                    order.setStatus("CANCELLED");
                                    orderService.updateOrder(order, oldStatus);
                                    log.info("Updated order {} status to CANCELLED after failed VNPay payment", order.getId());
                                }
                            } else {
                                log.warn("Order not found for payment update: {}", payment.getOrderId());
                            }
                        }
                        
                    } else {
                        log.warn("Payment record not found for VNPay TxnRef: {}", vnpTxnRef);
                    }
                } catch (Exception e) {
                    log.error("Error updating VNPay payment result", e);
                }
            }

            // Set model attributes
            model.addAttribute(VNPayConstants.ORDER_ID_ATTR, orderInfo);
            model.addAttribute(VNPayConstants.TOTAL_PRICE_ATTR, totalPriceVND);
            model.addAttribute(VNPayConstants.PAYMENT_TIME_ATTR, formattedDate);
            model.addAttribute(VNPayConstants.TRANSACTION_ID_ATTR, transactionId);
            model.addAttribute(VNPayConstants.PAYMENT_STATUS_ATTR, paymentStatus);

            // Add response message
            String responseMessage = VNPayConstants.getResponseMessage(vnpResponseCode != null ? vnpResponseCode : "99");
            model.addAttribute("responseMessage", responseMessage);

            if (paymentStatus == 0) {
                // Handle failed payment
                log.warn("VNPay payment failed - TxnRef: {}, ResponseCode: {}", vnpTxnRef, vnpResponseCode);
                model.addAttribute("errorMessage", VNPayConstants.MSG_PAYMENT_FAILED);
            } else if (paymentStatus == 1) {
                // Payment successful
                log.info("VNPay payment successful - TxnRef: {}", vnpTxnRef);
                model.addAttribute("successMessage", VNPayConstants.MSG_PAYMENT_SUCCESS);
            } else {
                // Invalid signature or other error
                log.error("VNPay payment error - Invalid signature or system error - TxnRef: {}", vnpTxnRef);
                model.addAttribute("errorMessage", VNPayConstants.MSG_INVALID_SIGNATURE);
            }

            return paymentStatus == 1 ? VNPayConstants.ORDER_SUCCESS_VIEW : VNPayConstants.ORDER_FAIL_VIEW;
            
        } catch (Exception e) {
            log.error("Error processing VNPay payment return", e);
            model.addAttribute("errorMessage", VNPayConstants.MSG_SYSTEM_ERROR);
            return VNPayConstants.ORDER_FAIL_VIEW;
        }
    }
}

