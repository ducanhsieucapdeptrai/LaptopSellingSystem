package com.example.computershop.service.impl;

import com.example.computershop.dto.CartItemDisplay;
import com.example.computershop.entity.*;
import com.example.computershop.exception.CartConstants;
import com.example.computershop.repository.CartRepository;
import com.example.computershop.repository.ProductRepository;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.service.CartService;
import com.example.computershop.service.ProductVariantService;
import com.example.computershop.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantService productVariantService;
    private final VoucherService voucherService;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository, 
                          ProductRepository productRepository, ProductVariantService productVariantService,
                          VoucherService voucherService) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productVariantService = productVariantService;
        this.voucherService = voucherService;
    }

    @Override
    public List<Cart> getCurrentUserCart(Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user != null) {
            return cartRepository.findByUser(user);
        }
        return new ArrayList<>();
    }

    @Override
    public User getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }

        // Handle OAuth2 authentication
        if (principal instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            String email = getEmailFromOAuth2Attributes(provider, oauth2User.getAttributes());
            
            if (email != null) {
                // OAuth2 users are stored with username = email
                return userRepository.findByUsername(email).orElse(null);
            }
            return null;
        }

        // Handle form authentication
        String identifier = principal.getName();
        return userRepository.findByUsernameOrEmail(identifier, identifier).orElse(null);
    }

    @Override
    public List<CartItemDisplay> convertToDisplayItems(List<Cart> cartItems) {
        return cartItems.stream()
                .map(cart -> {
                    if (cart.getVariant() != null) {
                        return new CartItemDisplay(
                            cart.getProduct(), 
                            cart.getVariant(), 
                            cart.getQuantity(),
                            cart.getVoucherCode(),
                            cart.getDiscountAmount()
                        );
                    } else {
                        return new CartItemDisplay(cart.getProduct(), cart.getQuantity());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<Map<String, Object>> addToCart(String productId, String variantId, Integer quantity, Principal principal) {
        try {
           
            if (productId == null || productId.trim().isEmpty()) {
                return createErrorResponse("Product ID cannot be empty", HttpStatus.BAD_REQUEST.value());
            }
            
            if (quantity == null || quantity <= 0 || quantity > 10) {
                return createErrorResponse("Quantity must be between 1 and 10", HttpStatus.BAD_REQUEST.value());
            }

            
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return createErrorResponse("Please login to add product to cart", HttpStatus.UNAUTHORIZED.value());
            }

           
            Products product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return createErrorResponse("Product not found", HttpStatus.NOT_FOUND.value());
            }

          
            ProductVariant variant = null;
            if (variantId != null && !variantId.trim().isEmpty()) {
                variant = productVariantService.findById(variantId);
                if (variant == null) {
                    return createErrorResponse("Variant not found", HttpStatus.NOT_FOUND.value());
                }
            }

       
            int availableStock = variant != null ? 
                (variant.getQuantity() != null ? variant.getQuantity() : 0) : 
                (product.getQuantity() != null ? product.getQuantity() : 0);

            if (availableStock < quantity) {
                return createErrorResponse("Not enough stock in stock. Remaining: " + availableStock, HttpStatus.BAD_REQUEST.value());
            }

         
            Optional<Cart> existingCartItem;
            if (variant != null) {
                existingCartItem = cartRepository.findByUserAndProductAndVariant(user, product, variant);
            } else {
                existingCartItem = cartRepository.findByUserAndProductAndVariantIsNull(user, product);
            }

            if (existingCartItem.isPresent()) {
                Cart cartItem = existingCartItem.get();
                int newQuantity = cartItem.getQuantity() + quantity;
                
                if (newQuantity > 10) {
                    return createErrorResponse("Cannot add. Maximum 10 products per type", HttpStatus.BAD_REQUEST.value());
                }
                
                if (newQuantity > availableStock) {
                    return createErrorResponse("Not enough stock in stock. Remaining: " + availableStock, HttpStatus.BAD_REQUEST.value());
                }
                
                cartItem.setQuantity(newQuantity);
                cartRepository.save(cartItem);
            } else {
                Cart newCartItem = new Cart();
                newCartItem.setUser(user);
                newCartItem.setProduct(product);
                newCartItem.setVariant(variant);
                newCartItem.setQuantity(quantity);
                newCartItem.setCreatedAt(LocalDateTime.now());
                cartRepository.save(newCartItem);
            }

        
            int cartCount = cartRepository.findByUser(user).stream()
                    .mapToInt(Cart::getQuantity)
                    .sum();

            Map<String, Object> data = new HashMap<>();
            data.put("cartCount", cartCount);
            data.put("message", "Product added to cart successfully");

            return createSuccessResponse(data);

        } catch (Exception e) {
            return createErrorResponse("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateCartItem(String productId, String variantId, Integer quantity, Principal principal) {
        try {
            if (quantity == null || quantity <= 0 || quantity > 10) {
                return createErrorResponse("Quantity must be between 1 and 10", HttpStatus.BAD_REQUEST.value());
            }

            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return createErrorResponse("Please login", HttpStatus.UNAUTHORIZED.value());
            }

            Products product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return createErrorResponse("Product not found", HttpStatus.NOT_FOUND.value());
            }

            ProductVariant variant = null;
            if (variantId != null && !variantId.trim().isEmpty()) {
                variant = productVariantService.findById(variantId);
            }

  
            Optional<Cart> cartItemOpt;
            if (variant != null) {
                cartItemOpt = cartRepository.findByUserAndProductAndVariant(user, product, variant);
            } else {
                cartItemOpt = cartRepository.findByUserAndProductAndVariantIsNull(user, product);
            }

            if (!cartItemOpt.isPresent()) {
                return createErrorResponse("Product not in cart", HttpStatus.NOT_FOUND.value());
            }

            Cart cartItem = cartItemOpt.get();
            
    
            int availableStock = variant != null ? 
                (variant.getQuantity() != null ? variant.getQuantity() : 0) : 
                (product.getQuantity() != null ? product.getQuantity() : 0);

            if (availableStock < quantity) {
                return createErrorResponse("Not enough stock in stock. Remaining: " + availableStock, HttpStatus.BAD_REQUEST.value());
            }

            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "Quantity updated successfully");
            return createSuccessResponse(data);

        } catch (Exception e) {
            return createErrorResponse("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> removeFromCart(String productId, String variantId, Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return createErrorResponse("Please login", HttpStatus.UNAUTHORIZED.value());
            }

            Products product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return createErrorResponse("Product not found", HttpStatus.NOT_FOUND.value());
            }

            ProductVariant variant = null;
            if (variantId != null && !variantId.trim().isEmpty()) {
                variant = productVariantService.findById(variantId);
            }

   
            Optional<Cart> cartItemOpt;
            if (variant != null) {
                cartItemOpt = cartRepository.findByUserAndProductAndVariant(user, product, variant);
            } else {
                cartItemOpt = cartRepository.findByUserAndProductAndVariantIsNull(user, product);
            }

            if (cartItemOpt.isPresent()) {
                cartRepository.delete(cartItemOpt.get());
                
                Map<String, Object> data = new HashMap<>();
                data.put("message", "Product removed from cart");
                return createSuccessResponse(data);
            } else {
                return createErrorResponse("Product not in cart", HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e) {
            return createErrorResponse("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getCartCount(Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            int cartCount = 0;
            
            if (user != null) {
                cartCount = cartRepository.findByUser(user).stream()
                        .mapToInt(Cart::getQuantity)
                        .sum();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("cartCount", cartCount);
            return createSuccessResponse(data);

        } catch (Exception e) {
            return createErrorResponse("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> applyVoucher(String voucherCode, Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return createErrorResponse("Please login", HttpStatus.UNAUTHORIZED.value());
            }

            if (voucherCode == null || voucherCode.trim().isEmpty()) {
                return createErrorResponse("Please enter voucher code", HttpStatus.BAD_REQUEST.value());
            }

    
            Optional<Voucher> voucherOpt = voucherService.getValidVoucherByCode(voucherCode.trim());
            if (!voucherOpt.isPresent()) {
                return createErrorResponse("Voucher code not found or expired", HttpStatus.NOT_FOUND.value());
            }
            
            Voucher voucher = voucherOpt.get();

            List<Cart> cartItems = cartRepository.findByUser(user);
            if (cartItems.isEmpty()) {
                return createErrorResponse("Cart is empty", HttpStatus.BAD_REQUEST.value());
            }

            Long subtotal = cartItems.stream()
                    .mapToLong(cart -> {
                        if (cart.getVariant() != null) {
                            return cart.getVariant().getPrice().longValue() * cart.getQuantity();
                        } else {
                            return cart.getProduct().getPrice().longValue() * cart.getQuantity();
                        }
                    })
                    .sum();

            Long discountAmount = voucherService.calculateDiscountAmount(voucher, subtotal);
      
            Long remainingDiscount = discountAmount;
            for (int i = 0; i < cartItems.size(); i++) {
                Cart cart = cartItems.get(i);
                Long itemTotal = cart.getVariant() != null ? 
                    cart.getVariant().getPrice().longValue() * cart.getQuantity() :
                    cart.getProduct().getPrice().longValue() * cart.getQuantity();
                
                Long itemDiscount;
                if (i == cartItems.size() - 1) {
                    // Last item gets remaining discount to avoid rounding issues
                    itemDiscount = remainingDiscount;
                } else {
                    itemDiscount = (discountAmount * itemTotal) / subtotal;
                    remainingDiscount -= itemDiscount;
                }
                
                cart.setVoucherCode(voucherCode.trim());
                cart.setDiscountAmount(itemDiscount);
                cartRepository.save(cart);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", "Voucher applied successfully");
            data.put("discountAmount", discountAmount);
            data.put("voucherCode", voucherCode.trim());
            return createSuccessResponse(data);

        } catch (Exception e) {
            return createErrorResponse("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> removeVoucher(Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return createErrorResponse("Please login", HttpStatus.UNAUTHORIZED.value());
            }

            List<Cart> cartItems = cartRepository.findByUser(user);
            for (Cart cart : cartItems) {
                cart.setVoucherCode(null);
                cart.setDiscountAmount(0L);
                cartRepository.save(cart);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("message", "Voucher removed successfully");
            return createSuccessResponse(data);

        } catch (Exception e) {
            return createErrorResponse("An error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public Map<String, Long> calculateCartTotals(List<Cart> cartItems) {
        Long subtotal = 0L;
        Long discountTotal = 0L;
        
        for (Cart cart : cartItems) {
            Long itemPrice = cart.getVariant() != null ? 
                cart.getVariant().getPrice().longValue() * cart.getQuantity() :
                cart.getProduct().getPrice().longValue() * cart.getQuantity();
            
            subtotal += itemPrice;
            discountTotal += (cart.getDiscountAmount() != null ? cart.getDiscountAmount() : 0L);
        }
        
        Long finalTotal = subtotal - discountTotal;
        
        Map<String, Long> totals = new HashMap<>();
        totals.put("subtotal", subtotal);
        totals.put("discount", discountTotal);
        totals.put("finalTotal", finalTotal);
        
        return totals;
    }

    @Override
    public void clearCart(User user) {
        List<Cart> cartItems = cartRepository.findByUser(user);
        cartRepository.deleteAll(cartItems);
    }

    @Override
    public ResponseEntity<Map<String, Object>> createErrorResponse(String message, int status) {
        Map<String, Object> response = new HashMap<>();
        response.put(CartConstants.SUCCESS, false);
        response.put(CartConstants.MESSAGE, message);
        return ResponseEntity.status(status).body(response);
    }

    @Override
    public ResponseEntity<Map<String, Object>> createSuccessResponse(Map<String, Object> data) {
        Map<String, Object> response = new HashMap<>();
        response.put(CartConstants.SUCCESS, true);
        response.putAll(data);
        return ResponseEntity.ok(response);
    }
    
    private String getEmailFromOAuth2Attributes(String provider, Map<String, Object> attributes) {
        try {
            if ("google".equals(provider)) {
                return (String) attributes.get("email");
            } else if ("github".equals(provider)) {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String username = (String) attributes.get("login");
                    if (username != null) {
                        email = username + "@github.com";
                    }
                }
                return email;
            } else if ("facebook".equals(provider)) {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String facebookId = (String) attributes.get("id");
                    if (facebookId != null) {
                        email = "facebook_" + facebookId + "@facebook.com";
                    }
                }
                return email;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    
    @Override
    public String addProductToCart(String productId, int quantity, Principal principal) {
        try {
            // Validate quantity
            if (quantity <= 0) {
                return "error:Quantity must be greater than 0!";
            }

            // Validate product
            String productError = validateProductAvailability(productId);
            if (productError != null) {
                return "error:" + productError;
            }

            // Validate user
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return "redirect:" + CartConstants.REDIRECT_AUTH_LOGIN;
            }

            // Process cart addition
            Object result = processCartAddition(user, productId, quantity, false);
            if (result instanceof String && ((String) result).startsWith("error:")) {
                return (String) result;
            }

            return CartConstants.REDIRECT_CART_VIEW;

        } catch (Exception e) {
            return "error:An error occurred when adding to cart: " + e.getMessage();
        }
    }
    
    @Override
    public String updateCartQuantity(String productId, int quantity, Principal principal) {
        try {
            // Validate quantity
            if (quantity <= 0) {
                return "error:Quantity must be greater than 0!";
            }

            // Validate user
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return "error:Please login to update cart!";
            }

            // Validate product and stock
            Products product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return "error:Product not found!";
            }

            if (quantity > product.getQuantity()) {
                return "error:Not enough stock in stock! Only " + product.getQuantity() + " products remaining.";
            }

            // Update cart
            List<Cart> userCart = cartRepository.findByUser(user);
            boolean updated = false;
            for (Cart c : userCart) {
                if (c.getProduct().getProductID().equals(productId)) {
                    c.setQuantity(quantity);
                    cartRepository.save(c);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                return "error:Product not in cart!";
            }

            return "success";
        } catch (Exception e) {
            return "error:An error occurred when updating cart: " + e.getMessage();
        }
    }
    
    @Override
    public String removeCartItem(String productId, Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return "error:Please login!";
            }

            List<Cart> userCart = cartRepository.findByUser(user);
            Cart itemToRemove = null;
            for (Cart c : userCart) {
                if (c.getProduct().getProductID().equals(productId)) {
                    itemToRemove = c;
                    break;
                }
            }

            if (itemToRemove != null) {
                cartRepository.delete(itemToRemove);
            }

            return "success";
        } catch (Exception e) {
            return "error:An error occurred when removing product: " + e.getMessage();
        }
    }
    
    @Override
    public String clearUserCart(Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user != null) {
                List<Cart> userCartItems = cartRepository.findByUser(user);
                cartRepository.deleteAll(userCartItems);
            }
            return "success";
        } catch (Exception e) {
            return "error:An error occurred when clearing cart: " + e.getMessage();
        }
    }
    
    @Override
    public String validateProductAvailability(String productId) {
        Products product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "Product not found!";
        }
        if (product.getIsActive() == null || !product.getIsActive()) {
            return "Product is not available!";
        }
        return null;
    }
    
    @Override
    public Object processCartAddition(User user, String productId, int quantity, boolean isAjax) {
        List<Cart> userCart = cartRepository.findByUser(user);
        Products product = productRepository.findById(productId).orElse(null);
        
        if (product == null) {
            String errorMsg = "Product not found!";
            if (isAjax) {
                return createErrorResponse(errorMsg, 400);
            }
            return "error:" + errorMsg;
        }

        int currentCartQuantity = 0;
        Cart existingCartItem = null;
        for (Cart c : userCart) {
            if (c.getProduct().getProductID().equals(productId)) {
                currentCartQuantity = c.getQuantity();
                existingCartItem = c;
                break;
            }
        }

        // Check stock
        int totalRequestedQuantity = currentCartQuantity + quantity;
        if (totalRequestedQuantity > product.getQuantity()) {
            String errorMsg = String.format("Not enough stock in stock! Currently only %d products, you have %d in cart.",
                    product.getQuantity(), currentCartQuantity);
            if (isAjax) {
                return createErrorResponse(errorMsg, 400);
            }
            return "error:" + errorMsg;
        }

        // Update or add cart item
        if (existingCartItem != null) {
            existingCartItem.setQuantity(totalRequestedQuantity);
            cartRepository.save(existingCartItem);
        } else {
            Cart newItem = new Cart();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUser(user);
            newItem.setCreatedAt(LocalDateTime.now());
            cartRepository.save(newItem);
        }

        // Return response
        if (isAjax) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put(CartConstants.MESSAGE, String.format("Added %s to cart!", product.getName()));
            int cartCount = cartRepository.findByUser(user).stream()
                    .mapToInt(Cart::getQuantity)
                    .sum();
            responseData.put(CartConstants.CART_COUNT, cartCount);
            return createSuccessResponse(responseData);
        }

        return "success";
    }
    
    
    @Override
    public String applyVoucherToCart(String voucherCode, Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return "error:Please login to use voucher";
            }

            List<Cart> userCart = cartRepository.findByUser(user);
            if (userCart.isEmpty()) {
                return "error:Cart is empty";
            }

            // Validate voucher
            Optional<Voucher> voucherOpt = voucherService.getValidVoucherByCode(voucherCode);
            if (!voucherOpt.isPresent()) {
                return "error:Voucher code is not valid or expired";
            }

            Voucher voucher = voucherOpt.get();

            // Calculate total original amount
            Long totalOriginal = calculateCartOriginalTotal(userCart);

            // Calculate discount
            Long discountAmount = voucherService.calculateDiscountAmount(voucher, totalOriginal);
            
            if (discountAmount <= 0) {
                return "error:Voucher can not be applied for this Order";
            }

            // Remove existing voucher if any
            removeVoucherFromAllCartItems(userCart);

            // Distribute discount across cart items proportionally
            distributeDiscountToCartItems(userCart, discountAmount, voucherCode);

            try {
                updateVoucherUsageTracking(voucher, user);
            } catch (Exception trackingError) {
                System.err.println("Warning: Failed to update voucher usage tracking, but voucher was applied successfully: " + trackingError.getMessage());
               
            }

            return "success:Applied voucher " + voucherCode + " successfully! Discount " + formatCurrency(discountAmount) + " VND";

        } catch (Exception e) {
            return "error:An error occurred when applying voucher: " + e.getMessage();
        }
    }
    
    @Override
    public String removeVoucherFromCart(Principal principal) {
        try {
            User user = getUserFromPrincipal(principal);
            if (user == null) {
                return "error:Please login!";
            }

            List<Cart> userCart = cartRepository.findByUser(user);
            removeVoucherFromAllCartItems(userCart);

            return "success:Removed voucher from cart";

        } catch (Exception e) {
            return "error:An error occurred when removing voucher: " + e.getMessage();
        }
    }
    
    @Override
    public Long calculateCartOriginalTotal(List<Cart> cartItems) {
        Long total = 0L;
        for (Cart item : cartItems) {
            total += item.getOriginalPrice();
        }
        return total;
    }
    
    @Override
    public void distributeDiscountToCartItems(List<Cart> cartItems, Long totalDiscount, String voucherCode) {
        Long totalOriginal = calculateCartOriginalTotal(cartItems);
        
        if (totalOriginal <= 0) return;

        Long distributedDiscount = 0L;
        
        for (int i = 0; i < cartItems.size(); i++) {
            Cart item = cartItems.get(i);
            Long itemOriginal = item.getOriginalPrice();
            Long itemDiscount;
            
            // For the last item, assign remaining discount to avoid rounding errors
            if (i == cartItems.size() - 1) {
                itemDiscount = totalDiscount - distributedDiscount;
            } else {
                itemDiscount = (itemOriginal * totalDiscount) / totalOriginal;
                distributedDiscount += itemDiscount;
            }
            
            item.setVoucherCode(voucherCode);
            item.setDiscountAmount(itemDiscount);
            cartRepository.save(item);
        }
    }
    
    @Override
    public void removeVoucherFromAllCartItems(List<Cart> cartItems) {
        for (Cart item : cartItems) {
            item.setVoucherCode(null);
            item.setDiscountAmount(0L);
            cartRepository.save(item);
        }
    }
    
    // Helper methods
    
    /**
     * Update voucher usage tracking
     */
    private void updateVoucherUsageTracking(Voucher voucher, User user) {
        try {
            // Reload voucher from database to get fresh data
            Voucher freshVoucher = voucherService.getVoucherById(voucher.getVoucherId());
            if (freshVoucher == null) {
                freshVoucher = voucher;
            }
            
            freshVoucher.setLastUsedBy(user.getUserId());
            freshVoucher.setLastUsedAt(LocalDateTime.now());
            
            // Handle null usageCount case with extra safety
            Integer currentUsageCount = safeGetUsageCount(freshVoucher);
            Integer newUsageCount = currentUsageCount + 1;
            freshVoucher.setUsageCount(newUsageCount);
            
            voucherService.updateVoucher(freshVoucher);
            
        } catch (Exception e) {
            // If update fails, just log and continue - don't break voucher application
            System.err.println("Failed to update voucher usage tracking: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
 
    private String formatCurrency(Long amount) {
        return String.format("%,d", amount);
    }
    

    private Integer safeGetUsageCount(Voucher voucher) {
        try {
            if (voucher == null) return 0;
            voucher.ensureProperInitialization();
            return voucher.getUsageCount();
        } catch (Exception e) {
            System.err.println("Error getting usage count: " + e.getMessage());
            return 0;
        }
    }

    
    @Override
    public Map<String, Object> prepareCartViewData(Principal principal) {
        Map<String, Object> viewData = new HashMap<>();
        
        try {
            List<Cart> userCart = getCurrentUserCart(principal);
            List<CartItemDisplay> displayItems = new ArrayList<>();
            Long originalTotal = 0L;
            Long finalTotal = 0L;
            Long totalDiscount = 0L;
            String voucherCode = null;
            boolean hasVoucher = false;

            for (Cart item : userCart) {
                Products product = productRepository.findById(item.getProduct().getProductID()).orElse(null);
                if (product != null) {
                    // Create CartItemDisplay with voucher information
                    CartItemDisplay displayItem = new CartItemDisplay(
                        product, 
                        item.getVariant(), 
                        item.getQuantity(), 
                        item.getVoucherCode(), 
                        item.getDiscountAmount()
                    );
                    displayItems.add(displayItem);
                    
                    originalTotal += item.getOriginalPrice();
                    finalTotal += item.getFinalPrice();
                    
                    if (item.getVoucherCode() != null && !item.getVoucherCode().isEmpty()) {
                        voucherCode = item.getVoucherCode();
                        hasVoucher = true;
                        totalDiscount += (item.getDiscountAmount() != null ? item.getDiscountAmount() : 0L);
                    }
                }
            }

            viewData.put("cartItems", displayItems);
            viewData.put("originalTotal", originalTotal);
            viewData.put("finalTotal", finalTotal);
            viewData.put("totalDiscount", totalDiscount);
            viewData.put("voucherCode", voucherCode);
            viewData.put("hasVoucher", hasVoucher);
            viewData.put("isEmpty", userCart.isEmpty());
            
        } catch (Exception e) {
            viewData.put("error", "An error occurred when loading cart: " + e.getMessage());
        }
        
        return viewData;
    }
    
    @Override
    public Map<String, Object> prepareCheckoutData(Principal principal) {
        Map<String, Object> checkoutData = prepareCartViewData(principal);
        
        // Add order object for form binding
        checkoutData.put("order", new Order());
        
        // Add total for compatibility
        checkoutData.put("total", checkoutData.get("finalTotal"));
        
        return checkoutData;
    }
    
    @Override
    public Map<String, Object> prepareCartReviewData(Principal principal) {
        Map<String, Object> reviewData = new HashMap<>();
        
        try {
            List<Cart> userCart = getCurrentUserCart(principal);
            List<CartItemDisplay> displayItems = new ArrayList<>();
            Long finalTotal = 0L;

            for (Cart item : userCart) {
                Products product = productRepository.findById(item.getProduct().getProductID()).orElse(null);
                if (product != null) {
                    // Create CartItemDisplay with voucher information
                    CartItemDisplay displayItem = new CartItemDisplay(
                        product, 
                        item.getVariant(), 
                        item.getQuantity(), 
                        item.getVoucherCode(), 
                        item.getDiscountAmount()
                    );
                    displayItems.add(displayItem);
                    finalTotal += item.getFinalPrice();
                }
            }

            reviewData.put("cartItems", displayItems);
            reviewData.put("total", finalTotal);
            
        } catch (Exception e) {
            reviewData.put("error", "An error occurred when loading cart review: " + e.getMessage());
        }
        
        return reviewData;
    }

    
    @Override
    public User getUserById(UUID userId) {
        return userRepository.findById(userId.toString()).orElse(null);
    }
    
    @Override
    public List<Cart> getCurrentUserCartByUserId(UUID userId) {
        User user = getUserById(userId);
        if (user != null) {
            return cartRepository.findByUser(user);
        }
        return new ArrayList<>();
    }
    
    @Override
    public void clearCartByUserId(UUID userId) {
        User user = getUserById(userId);
        if (user != null) {
            clearCart(user);
        }
    }
} 