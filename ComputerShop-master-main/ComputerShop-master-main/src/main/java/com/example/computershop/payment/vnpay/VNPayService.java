package com.example.computershop.payment.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class VNPayService {
    
    /**
     * Create VNPay payment URL with proper transaction reference tracking (long amount)
     */
    public VNPayPaymentRequest createOrder(HttpServletRequest request, long amount, String urlReturn, String vnpTxnRef){
        // Validate amount first
        if (!VNPayConstants.isValidAmount(amount)) {
            String errorMessage = VNPayConstants.getAmountValidationError(amount);
            log.error("Invalid VNPay amount: {} VND. Error: {}", amount, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        
        // Convert to int safely after validation
        return createOrderInternal(request, (int) amount, urlReturn, vnpTxnRef);
    }
    
    /**
     * Create VNPay payment URL with proper transaction reference tracking (int amount - for backward compatibility)
     */
    public VNPayPaymentRequest createOrder(HttpServletRequest request, int amount, String urlReturn, String vnpTxnRef){
        return createOrderInternal(request, amount, urlReturn, vnpTxnRef);
    }
    
    /**
     * Internal method to create VNPay payment URL
     */
    private VNPayPaymentRequest createOrderInternal(HttpServletRequest request, int amount, String urlReturn, String vnpTxnRef){
        try {
            // Validate amount first
            long amountVND = amount;
            if (!VNPayConstants.isValidAmount(amountVND)) {
                String errorMessage = VNPayConstants.getAmountValidationError(amountVND);
                log.error("Invalid VNPay amount: {} VND. Error: {}", amountVND, errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            
            log.info("Creating VNPay order for amount: {} VND", amountVND);
            
            //Các bạn có thể tham khảo tài liệu hướng dẫn và điều chỉnh các tham số
            String vnp_Version = VNPayConstants.VNP_VERSION;
            String vnp_Command = VNPayConstants.VNP_COMMAND_PAY;
            // Use provided vnpTxnRef or generate new one
            if (vnpTxnRef == null || vnpTxnRef.isEmpty()) {
                vnpTxnRef = VNPayConfig.getRandomNumber(8);
            }
            String vnp_IpAddr = VNPayConfig.getIpAddress(request);
            String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
            String orderType = "order-type";

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            
            // VNPay requires amount in xu (VND * 100) format
            long amountXu = amountVND * VNPayConstants.AMOUNT_MULTIPLIER;
            String totalAmount = String.valueOf(amountXu);
            log.info("VNPay amount conversion: {} VND -> {} xu", amountVND, amountXu);
            
            vnp_Params.put("vnp_Amount", totalAmount);
            vnp_Params.put("vnp_CurrCode", VNPayConstants.VNP_CURRENCY_VND);

            vnp_Params.put("vnp_TxnRef", vnpTxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnpTxnRef);
            vnp_Params.put("vnp_OrderType", orderType);

            String locate = VNPayConstants.VNP_LOCALE_VN;
            vnp_Params.put("vnp_Locale", locate);

            urlReturn += VNPayConstants.VNPAY_RETURN_URL_PATH;
            vnp_Params.put("vnp_ReturnUrl", urlReturn);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat(VNPayConstants.VNPAY_DATE_FORMAT);
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            // Set expiry time
            cld.add(Calendar.MINUTE, VNPayConstants.TRANSACTION_EXPIRY_MINUTES);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    try {
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                        //Build query
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                        query.append('=');
                        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    } catch (UnsupportedEncodingException e) {
                        log.error("Error encoding VNPay parameters", e);
                        throw new RuntimeException("Failed to encode VNPay parameters", e);
                    }
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            
            String queryUrl = query.toString();
            String salt = VNPayConfig.vnp_HashSecret;
            String vnp_SecureHash = VNPayConfig.hmacSHA512(salt, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
            
            log.info("Created VNPay payment URL for TxnRef: {}, Amount: {}", vnpTxnRef, amount);
            
            // Return both URL and transaction reference
            return new VNPayPaymentRequest(paymentUrl, vnpTxnRef);
            
        } catch (Exception e) {
            log.error("Error creating VNPay order", e);
            throw new RuntimeException("Failed to create VNPay payment URL", e);
        }
    }
    
    /**
     * Backward compatibility method
     */
    public String createOrder(HttpServletRequest request, int amount, String urlReturn) {
        VNPayPaymentRequest paymentRequest = createOrder(request, amount, urlReturn, null);
        return paymentRequest.getPaymentUrl();
    }

    public int orderReturn(HttpServletRequest request){
        try {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = null;
                String fieldValue = null;
                try {
                    fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII.toString());
                    fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
                } catch (UnsupportedEncodingException e) {
                    log.error("Error encoding return parameters", e);
                }
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, fieldValue);
                }
            }

            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            String vnp_TxnRef = request.getParameter("vnp_TxnRef");
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus");
            
            log.info("Processing VNPay return - TxnRef: {}, ResponseCode: {}, TransactionStatus: {}", 
                    vnp_TxnRef, vnp_ResponseCode, vnp_TransactionStatus);
            
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }
            
            String signValue = VNPayConfig.hashAllFields(fields);
            if (signValue.equals(vnp_SecureHash)) {
                if (VNPayConstants.SUCCESS_CODE.equals(vnp_TransactionStatus)) {
                    log.info("VNPay payment successful - TxnRef: {}", vnp_TxnRef);
                    return 1; // Success
                } else {
                    log.warn("VNPay payment failed - TxnRef: {}, ResponseCode: {}", vnp_TxnRef, vnp_ResponseCode);
                    return 0; // Failed
                }
            } else {
                log.error("VNPay invalid signature - TxnRef: {}. Expected: {}, Received: {}", 
                         vnp_TxnRef, signValue, vnp_SecureHash);
                return -1; // Invalid signature
            }
            
        } catch (Exception e) {
            log.error("Error processing VNPay return", e);
            return -1; // Error
        }
    }
    
    /**
     * Helper class to return both payment URL and transaction reference
     */
    public static class VNPayPaymentRequest {
        private final String paymentUrl;
        private final String vnpTxnRef;
        
        public VNPayPaymentRequest(String paymentUrl, String vnpTxnRef) {
            this.paymentUrl = paymentUrl;
            this.vnpTxnRef = vnpTxnRef;
        }
        
        public String getPaymentUrl() {
            return paymentUrl;
        }
        
        public String getVnpTxnRef() {
            return vnpTxnRef;
        }
    }
}
