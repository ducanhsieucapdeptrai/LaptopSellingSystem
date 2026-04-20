/**
 * Homepage Login Required Handler
 * Handles add to cart functionality and login modal for non-authenticated users
 */
document.addEventListener('DOMContentLoaded', function() {
    // Wait for common utilities to be loaded
    if (typeof window.ComputerShop === 'undefined') {
        return;
    }
    
    // Handle all "Add to Cart" buttons
    const addToCartButtons = document.querySelectorAll('a[href*="/cart/add/"]');
    
    addToCartButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            const cartUrl = this.href;
            const productId = cartUrl.split('/').pop();
            
            // Add loading state to button
            if (window.ComputerShop && window.ComputerShop.Loading) {
                window.ComputerShop.Loading.showButtonLoading(this, 'Đang thêm...');
            } else {
                this.setAttribute('data-original-text', this.textContent);
                this.textContent = 'Đang thêm...';
                this.disabled = true;
            }
            
            // Make AJAX request to add to cart
            fetch(cartUrl, {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
            .then(response => {
                // Debug: Log response details
                console.log('Response status:', response.status);
                console.log('Response ok:', response.ok);
                console.log('Response headers:', Object.fromEntries(response.headers.entries()));
                
                // Remove loading state
                if (window.ComputerShop && window.ComputerShop.Loading) {
                    window.ComputerShop.Loading.hideButtonLoading(this);
                } else {
                    this.textContent = this.getAttribute('data-original-text') || 'Add to Cart';
                    this.disabled = false;
                }
                
                // Handle different status codes
                if (response.status === 401) {
                    console.log('401 detected, showing login alert');
                    // User not logged in, show simple alert with login button
                    showLoginAlert();
                    return;
                }
                
                // Only parse JSON for successful responses
                if (response.ok) {
                    return response.json();
                } else {
                    // Try to parse error response as JSON first
                    return response.json().then(errorData => {
                        throw new Error(errorData.message || 'An error occurred while adding to cart!');
                    }).catch(parseError => {
                        console.log('Error parsing response JSON:', parseError);
                        throw new Error('An error occurred while adding to cart!');
                    });
                }
            })
            .then(data => {
                console.log('=== AJAX SUCCESS RESPONSE ===');
                console.log('Data:', data);
                
                if (!data) return;
                
                if (data.success) {
                    console.log('SUCCESS: Adding product to cart');
                    showSuccessAlert(data.message || 'Product added to cart successfully!');
                    
                    if (data.cartCount !== undefined) {
                        console.log('Updating cart count to:', data.cartCount);
                        updateCartCount(data.cartCount);
                    }
                } else {
                    console.log('ERROR: Server returned success=false');
                    showErrorAlert(data.message || 'An error occurred while adding to cart!');
                }
            })
            .catch(error => {
                console.error('=== AJAX CATCH ERROR ===');
                console.error('Error:', error);
                
                // Remove loading state on error
                if (window.ComputerShop && window.ComputerShop.Loading) {
                    window.ComputerShop.Loading.hideButtonLoading(this);
                } else {
                    this.textContent = this.getAttribute('data-original-text') || 'Add to Cart';
                    this.disabled = false;
                }
                
                showErrorAlert('An error occurred while adding to cart!');
            });
        });
    });
    
    // Initialize homepage specific features
    initializeHomepageFeatures();
    
    function initializeHomepageFeatures() {
        // Add hover effects to product cards
        const productCards = document.querySelectorAll('.axil-product');
        productCards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-5px)';
                this.style.transition = 'transform 0.3s ease';
                this.style.boxShadow = '0 8px 25px rgba(0,0,0,0.1)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0)';
                this.style.boxShadow = '';
            });
        });
        
        // Initialize lazy loading for images if IntersectionObserver is supported
        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        if (img.dataset.src) {
                            img.src = img.dataset.src;
                            img.classList.remove('lazy');
                            imageObserver.unobserve(img);
                        }
                    }
                });
            });
            
            const lazyImages = document.querySelectorAll('img[loading="lazy"]');
            lazyImages.forEach(img => imageObserver.observe(img));
        }
        
        // Initialize brand carousel enhancements
        initializeBrandCarousel();
        
        // Initialize product sections animations
        initializeProductSections();
    }
    
    function initializeBrandCarousel() {
        const brandItems = document.querySelectorAll('.categrie-product');
        brandItems.forEach((item, index) => {
            // Add staggered animation delay
            item.style.animationDelay = `${index * 0.1}s`;
            
            // Add click analytics (if needed)
            item.addEventListener('click', function() {
                const brandName = this.querySelector('.cat-title')?.textContent;
                // Add analytics tracking here if needed
            });
        });
    }
    
    function initializeProductSections() {
        // Add scroll-triggered animations for product sections
        const sections = document.querySelectorAll('.axil-product-area, .axil-most-sold-product');
        
        if ('IntersectionObserver' in window) {
            const sectionObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.classList.add('animate-in');
                        // Animate child products with stagger
                        const products = entry.target.querySelectorAll('.axil-product, .axil-product-list');
                        products.forEach((product, index) => {
                            setTimeout(() => {
                                product.style.opacity = '1';
                                product.style.transform = 'translateY(0)';
                            }, index * 100);
                        });
                    }
                });
            }, {
                threshold: 0.1,
                rootMargin: '50px'
            });
            
            sections.forEach(section => {
                sectionObserver.observe(section);
                // Initially hide products for animation
                const products = section.querySelectorAll('.axil-product, .axil-product-list');
                products.forEach(product => {
                    product.style.opacity = '0';
                    product.style.transform = 'translateY(30px)';
                    product.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
                });
            });
        }
    }
    
    // Handle modal close events
    const loginModal = document.getElementById('loginRequiredModal');
    if (loginModal) {
        loginModal.addEventListener('hidden.bs.modal', function () {
            // Reset any states when modal is closed
        });
        
        loginModal.addEventListener('shown.bs.modal', function () {
            // Focus on login button when modal is shown
            const loginButton = this.querySelector('a[href*="/auth/login"]');
            if (loginButton) {
                loginButton.focus();
            }
        });
    }
    
    // Helper functions
    function showLoginAlert() {
        const modal = document.getElementById('loginRequiredModal');
        if (modal) {
            // Use Bootstrap modal if available
            if (typeof bootstrap !== 'undefined') {
                const bsModal = new bootstrap.Modal(modal);
                bsModal.show();
            } else if (window.ComputerShop && window.ComputerShop.Modal) {
                window.ComputerShop.Modal.show('loginRequiredModal');
            } else {
                // Fallback to simple alert
                if (confirm('Please log in to add to cart!\n\nDo you want to go to the login page?')) {
                    window.location.href = '/auth/login';
                }
            }
        } else {
            // Simple confirm dialog
            if (confirm('Please log in to add to cart!\n\nDo you want to go to the login page?')) {
                window.location.href = '/auth/login';
            }
        }
    }
    
    function showSuccessAlert(message) {
        if (window.ComputerShop && window.ComputerShop.Notification) {
            window.ComputerShop.Notification.showSuccess(message);
        } else {
            alert(message);
        }
    }
    
    function showErrorAlert(message) {
        if (window.ComputerShop && window.ComputerShop.Notification) {
            window.ComputerShop.Notification.showError(message);
        } else {
            alert(message);
        }
    }
    
    function updateCartCount(count) {
        if (window.ComputerShop && window.ComputerShop.Cart) {
            window.ComputerShop.Cart.updateCartCount(count);
        } else {
            // Simple cart count update
            const cartCountElements = document.querySelectorAll('.cart-count');
            cartCountElements.forEach(el => {
                el.textContent = count;
            });
        }
    }
}); 