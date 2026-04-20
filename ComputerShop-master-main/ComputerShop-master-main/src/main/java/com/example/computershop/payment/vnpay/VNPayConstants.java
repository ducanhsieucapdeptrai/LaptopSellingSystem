package com.example.computershop.payment.vnpay;

/**
 * VNPay Constants for payment integration
 */
public final class VNPayConstants {
    
    // Private constructor to prevent instantiation
    private VNPayConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    
    // VNPay API URLs
    public static final String SANDBOX_PAYMENT_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static final String PRODUCTION_PAYMENT_URL = "https://vnpayment.vn/paymentv2/vpcpay.html";
    public static final String SANDBOX_API_URL = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    public static final String PRODUCTION_API_URL = "https://vnpayment.vn/merchant_webapi/api/transaction";
    
    // VNPay Response Codes
    public static final String SUCCESS_CODE = "00";
    public static final String TRANSACTION_SUCCESSFUL = "00";
    public static final String SUSPICIOUS_TRANSACTION = "07";
    public static final String INVALID_AMOUNT = "04";
    public static final String INVALID_MERCHANT = "02";
    public static final String TRANSACTION_NOT_FOUND = "91";
    public static final String DUPLICATE_TRANSACTION = "94";
    public static final String INVALID_SIGNATURE = "97";
    public static final String SYSTEM_ERROR = "99";
    
    // Payment Status
    public static final String PAYMENT_STATUS_PENDING = "Pending";
    public static final String PAYMENT_STATUS_PAID = "Paid";
    public static final String PAYMENT_STATUS_FAILED = "Failed";
    public static final String PAYMENT_STATUS_CANCELLED = "Cancelled";
    
    // Payment Method
    public static final String PAYMENT_METHOD_VNPAY = "VNPAY";
    public static final String PAYMENT_METHOD_COD = "COD";
    
    // Order Status
    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_PAYMENT_PENDING = "PAYMENT_PENDING";
    public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    public static final String ORDER_STATUS_PROCESSING = "PROCESSING";
    public static final String ORDER_STATUS_SHIPPED = "SHIPPED";
    public static final String ORDER_STATUS_DELIVERED = "DELIVERED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    
    // VNPay Parameters
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND_PAY = "pay";
    public static final String VNP_CURRENCY_VND = "VND";
    public static final String VNP_LOCALE_VN = "vn";
    public static final String VNP_LOCALE_EN = "en";
    
    // Bank Codes
    public static final String BANK_CODE_VNPAYQR = "VNPAYQR";
    public static final String BANK_CODE_VNBANK = "VNBANK";
    public static final String BANK_CODE_INTCARD = "INTCARD";
    
    // URL Paths
    public static final String VNPAY_RETURN_URL_PATH = "/vnpay-payment-return";
    public static final String VNPAY_IPN_URL_PATH = "/vnpay/ipn";
    public static final String CHECKOUT_VNPAY_PATH = "/user/checkout/vnpay";
    
    // View Names
    public static final String ORDER_SUCCESS_VIEW = "user/orderSuccess";
    public static final String ORDER_FAIL_VIEW = "user/orderFail";
    public static final String CHECKOUT_VIEW = "Cart/checkout";
    
    // Model Attributes
    public static final String ORDER_ID_ATTR = "orderId";
    public static final String TOTAL_PRICE_ATTR = "totalPrice";
    public static final String PAYMENT_TIME_ATTR = "paymentTime";
    public static final String TRANSACTION_ID_ATTR = "transactionId";
    public static final String PAYMENT_STATUS_ATTR = "paymentStatus";
    
    // Error Messages
    public static final String MSG_INVALID_SIGNATURE = "Chữ ký không hợp lệ";
    public static final String MSG_ORDER_NOT_FOUND = "Không tìm thấy đơn hàng";
    public static final String MSG_PAYMENT_FAILED = "Thanh toán thất bại";
    public static final String MSG_PAYMENT_SUCCESS = "Thanh toán thành công";
    public static final String MSG_SYSTEM_ERROR = "Lỗi hệ thống";
    public static final String MSG_DUPLICATE_TRANSACTION = "Giao dịch trùng lặp";
    
    // Success Messages
    public static final String MSG_PAYMENT_PROCESSING = "Đang xử lý thanh toán";
    public static final String MSG_ORDER_CONFIRMED = "Đơn hàng đã được xác nhận";
    
    // VNPay Response Messages
    public static final String getResponseMessage(String responseCode) {
        switch (responseCode) {
            case SUCCESS_CODE:
                return "Giao dịch thành công";
            case "01":
                return "Giao dịch chưa hoàn tất";
            case "02":
                return "Giao dịch bị lỗi";
            case "04":
                return "Giao dịch đảo (Khách hàng đã bị trừ tiền tại Ngân hàng nhưng GD chưa thành công ở VNPAY)";
            case "05":
                return "VNPAY đang xử lý giao dịch này (GD hoàn tiền)";
            case "06":
                return "VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng (GD hoàn tiền)";
            case SUSPICIOUS_TRANSACTION:
                return "Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "GD Hoàn trả bị từ chối";
            case "10":
                return "Đã giao hàng";
            case "11":
                return "Giao dịch không thành công do: Khách hàng nhập sai mật khẩu thanh toán quá số lần quy định";
            case "12":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "13":
                return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            case INVALID_SIGNATURE:
                return "Chữ ký không hợp lệ";
            case SYSTEM_ERROR:
                return "Lỗi không xác định";
            default:
                return "Lỗi không xác định";
        }
    }
    
    // Date Time Formats
    public static final String VNPAY_DATE_FORMAT = "yyyyMMddHHmmss";
    public static final String DISPLAY_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    
    // Transaction Expiry Time (in minutes)
    public static final int TRANSACTION_EXPIRY_MINUTES = 15;
    
    // Amount multiplier (VNPay uses VND * 100)
    public static final int AMOUNT_MULTIPLIER = 100;
    
    // VNPay Amount Limits (in VND)
    public static final long MIN_AMOUNT_VND = 5_000L;           // 5,000 VND minimum
    public static final long MAX_AMOUNT_VND = 999_999_999L;     // Just under 1 billion VND maximum
    
    // VNPay Amount Limits (in xu - VND * 100)
    public static final long MIN_AMOUNT_XU = MIN_AMOUNT_VND * AMOUNT_MULTIPLIER;   // 500,000 xu
    public static final long MAX_AMOUNT_XU = MAX_AMOUNT_VND * AMOUNT_MULTIPLIER;   // 99,999,999,900 xu
    
    // Error Messages for Amount Validation
    public static final String MSG_AMOUNT_TOO_SMALL = "Số tiền thanh toán tối thiểu là 5,000 VND";
    public static final String MSG_AMOUNT_TOO_LARGE = "Số tiền thanh toán tối đa là 999,999,999 VND";
    
    /**
     * Format currency for error messages
     */
    private static String formatCurrency(long amount) {
        return String.format("%,d", amount);
    }
    
    /**
     * Validate VNPay amount in VND
     */
    public static boolean isValidAmount(long amountVND) {
        return amountVND >= MIN_AMOUNT_VND && amountVND <= MAX_AMOUNT_VND;
    }
    
    /**
     * Get validation error message for invalid amount
     */
    public static String getAmountValidationError(long amountVND) {
        if (amountVND < MIN_AMOUNT_VND) {
            return MSG_AMOUNT_TOO_SMALL;
        } else if (amountVND > MAX_AMOUNT_VND) {
            return MSG_AMOUNT_TOO_LARGE;
        }
        return null;
    }
} 