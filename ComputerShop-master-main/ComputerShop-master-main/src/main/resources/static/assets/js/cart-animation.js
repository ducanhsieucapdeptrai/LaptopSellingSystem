// Cart Animation JavaScript

document.addEventListener('DOMContentLoaded', function() {
    
    
    // Handle all add to cart buttons
    initializeAddToCartButtons();
    
    // Initialize cart review functionality
    initializeCartReview();
});

function initializeAddToCartButtons() {
    // Find all add to cart links
    const addToCartLinks = document.querySelectorAll('.select-option a[href*="/cart/add/"]');
    
    addToCartLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            
            const button = this;
            const href = button.href;
            const productId = extractProductId(href);
            
            if (!productId) return;
            
            // Start animation
            button.classList.add('loading', 'add-to-cart-btn');
            const originalText = button.textContent;
            button.textContent = 'Adding...';
            
            // Get button position
            const buttonRect = button.getBoundingClientRect();
            const startX = buttonRect.left + buttonRect.width / 2;
            const startY = buttonRect.top + buttonRect.height / 2;
            
            // Create water drop animation
            createWaterDrop(startX, startY, function() {
                // After animation, add to cart via AJAX
                addToCartAjax(productId, button, originalText);
            });
        });
    });
}

function extractProductId(href) {
    const match = href.match(/\/cart\/add\/([^/?]+)/);
    return match ? match[1] : null;
}

function addToCartAjax(productId, button, originalText) {
    fetch(`/cart/add/${productId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: 'sl=1'
    })
    .then(response => {
        // Handle authentication required
        if (response.status === 401) {
            // User not logged in, show login modal
            button.classList.remove('loading');
            button.textContent = originalText;
            showLoginRequiredModal();
            return null;
        }
        
        // Only parse JSON for successful responses
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('An error occurred while adding to the cart!');
        }
    })
    .then(data => {
        if (!data) return; // Skip if authentication required
        if (data.success) {
            // Update cart count
            if (data.cartCount !== undefined) {
                const cartCountElements = document.querySelectorAll('.cart-count');
                cartCountElements.forEach(el => {
                    el.textContent = data.cartCount;
                });
            }
            
            // Update cart review content
            updateCartReviewContent();
            
            // Show success message
            showSuccessMessage(data.message || 'Successfully added to cart!');
            
            // Reset button with success state
            button.classList.remove('loading');
            button.classList.add('success');
            button.innerHTML = '<i class="fas fa-check"></i> Added!';
            
            // Reset to original text after 2 seconds
            setTimeout(() => {
                button.classList.remove('success');
                button.textContent = originalText;
            }, 2000);
        } else {
            // Show error message
            showErrorMessage(data.message || 'An error occurred!');
            
            // Reset button
            button.classList.remove('loading');
            button.textContent = originalText;
        }
    })
    .catch(error => {
        
        showErrorMessage('An error occurred. Please try again!');
        
        // Reset button
        button.classList.remove('loading');
        button.textContent = originalText;
    });
}

function createWaterDrop(startX, startY, callback) {
    // Create water drop element
    const drop = document.createElement('div');
    drop.className = 'water-drop';
    drop.style.left = startX + 'px';
    drop.style.top = startY + 'px';
    document.body.appendChild(drop);
    
    // Get cart icon position
    const cartIcon = document.querySelector('.shopping-cart');
    if (cartIcon) {
        const cartRect = cartIcon.getBoundingClientRect();
        const endX = cartRect.left + cartRect.width / 2;
        const endY = cartRect.top + cartRect.height / 2;
        
        // Animate water drop
        animateWaterDrop(drop, startX, startY, endX, endY, function() {
            // Remove drop
            drop.remove();
            
            // Add ripple effect to cart
            addCartRipple();
            
            // Update cart count animation
            updateCartCountAnimation();
            
            // Call callback
            if (callback) callback();
        });
    } else {
        // If no cart icon found, just continue
        drop.remove();
        if (callback) callback();
    }
}

function animateWaterDrop(drop, startX, startY, endX, endY, callback) {
    const duration = 600; // milliseconds
    const startTime = Date.now();
    
    function animate() {
        const currentTime = Date.now();
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        // Easing function for smooth animation
        const easeOutQuad = progress * (2 - progress);
        
        // Calculate current position with parabolic path
        const currentX = startX + (endX - startX) * easeOutQuad;
        const currentY = startY + (endY - startY) * easeOutQuad - Math.sin(progress * Math.PI) * 50;
        
        drop.style.left = currentX + 'px';
        drop.style.top = currentY + 'px';
        
        // Apply scale and opacity animation
        const scale = 1 - progress * 0.7;
        const opacity = 1 - progress * 0.3;
        drop.style.transform = `rotate(45deg) scale(${scale})`;
        drop.style.opacity = opacity;
        
        if (progress < 1) {
            requestAnimationFrame(animate);
        } else {
            if (callback) callback();
        }
    }
    
    requestAnimationFrame(animate);
}

function addCartRipple() {
    const cartIcon = document.querySelector('.shopping-cart');
    if (cartIcon) {
        const ripple = document.createElement('div');
        ripple.className = 'cart-ripple';
        cartIcon.style.position = 'relative';
        cartIcon.appendChild(ripple);
        
        setTimeout(() => ripple.remove(), 600);
    }
}

function updateCartCountAnimation() {
    const cartCount = document.querySelector('.cart-count');
    if (cartCount) {
        cartCount.classList.add('cart-count-updated');
        setTimeout(() => cartCount.classList.remove('cart-count-updated'), 500);
    }
}

function showSuccessMessage(message) {
    const messageEl = document.createElement('div');
    messageEl.className = 'add-to-cart-success';
    messageEl.innerHTML = `
        <i class="fas fa-check-circle"></i> ${message}
    `;
    document.body.appendChild(messageEl);
    
    // Show message
    setTimeout(() => messageEl.classList.add('show'), 10);
    
    // Hide and remove message
    setTimeout(() => {
        messageEl.classList.remove('show');
        setTimeout(() => messageEl.remove(), 300);
    }, 3000);
}

function showErrorMessage(message) {
    const messageEl = document.createElement('div');
    messageEl.className = 'add-to-cart-success';
    messageEl.style.background = '#dc3545';
    messageEl.innerHTML = `
        <i class="fas fa-exclamation-circle"></i> ${message}
    `;
    document.body.appendChild(messageEl);
    
    // Show message
    setTimeout(() => messageEl.classList.add('show'), 10);
    
    // Hide and remove message
    setTimeout(() => {
        messageEl.classList.remove('show');
        setTimeout(() => messageEl.remove(), 300);
    }, 3000);
}

function showLoginRequiredModal() {
    // Try to use Bootstrap modal if available
    const modal = document.getElementById('loginRequiredModal');
    if (modal) {
        // Use Bootstrap 5 modal
        if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
        } 
        // Use Bootstrap 4 modal (jQuery)
        else if (typeof $ !== 'undefined' && $.fn.modal) {
            $(modal).modal('show');
        }
        // Fallback to simple alert
        else {
            alert('Please log in to add products to your shopping cart!');
        }
    } else {
        // Fallback if modal not found
        alert('Please log in to add products to your shopping cart!');
    }
}

function initializeCartReview() {
    // Handle cart dropdown button click
    const cartDropdownBtn = document.querySelector('.cart-dropdown-btn');
    const cartDropdown = document.getElementById('cart-dropdown');
    const cartClose = document.querySelector('.cart-close');
    
    if (cartDropdownBtn && cartDropdown) {
        cartDropdownBtn.addEventListener('click', function(e) {
            e.preventDefault();
            showCartReview();
        });
    }
    
    if (cartClose && cartDropdown) {
        cartClose.addEventListener('click', function() {
            hideCartReview();
        });
    }
    
    // Prevent cart content clicks from closing the review
    if (cartDropdown) {
        const cartContent = cartDropdown.querySelector('.cart-content-wrap');
        if (cartContent) {
            cartContent.addEventListener('click', function(e) {
                e.stopPropagation();
            });
        }
    }
    
    // Handle remove product from cart review
    if (cartDropdown) {
        const removeButtons = cartDropdown.querySelectorAll('.close-btn');
        removeButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const productId = this.getAttribute('data-product-id');
                if (productId) {
                    removeFromCartReview(productId, this);
                }
            });
        });
    }
    
    // Close cart review with Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            hideCartReview();
        }
    });
    
    // Close cart review when clicking outside of it
    document.addEventListener('click', function(e) {
        if (cartDropdown && cartDropdown.classList.contains('show')) {
            const isClickInsideCart = cartDropdown.contains(e.target);
            const isCartButton = cartDropdownBtn && cartDropdownBtn.contains(e.target);
            
            if (!isClickInsideCart && !isCartButton) {
                hideCartReview();
            }
        }
    });
}

function showCartReview() {
    const cartDropdown = document.getElementById('cart-dropdown');
    
    if (cartDropdown) {
        cartDropdown.classList.add('show');
    }
}

function hideCartReview() {
    const cartDropdown = document.getElementById('cart-dropdown');
    
    if (cartDropdown) {
        cartDropdown.classList.remove('show');
    }
}

function removeFromCartReview(productId, button) {
    // Show loading state
    button.style.opacity = '0.5';
    button.style.pointerEvents = 'none';
    
    // Make request to remove item
    fetch(`/cart/remove/${productId}`, {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => {
        if (response.ok) {
            // Update cart count in header
            updateCartCountFromServer();
            
            // Update cart review content
            updateCartReviewContent();
            
            showSuccessMessage('Successfully removed product from cart!');
        } else {
            throw new Error('Failed to remove item');
        }
    })
    .catch(error => {
        showErrorMessage('An error occurred while removing the product!');
        
        // Restore button state
        button.style.opacity = '';
        button.style.pointerEvents = '';
    });
}

function updateCartReview() {
    // Recalculate subtotal
    let total = 0;
    const cartItems = document.querySelectorAll('.cart-item');
    
    cartItems.forEach(item => {
        const priceText = item.querySelector('.item-price span:first-child').textContent;
        const quantityText = item.querySelector('.quantity-value').textContent;
        
        const price = parseFloat(priceText.replace(/[^0-9]/g, ''));
        const quantity = parseInt(quantityText);
        
        if (!isNaN(price) && !isNaN(quantity)) {
            total += price * quantity;
            
            // Update item total
            const itemTotal = item.querySelector('.item-total span:nth-child(2)');
            if (itemTotal) {
                itemTotal.textContent = new Intl.NumberFormat('vi-VN').format(price * quantity);
            }
        }
    });
    
    // Update subtotal
    const subtotalAmount = document.querySelector('.subtotal-amount span:first-child');
    if (subtotalAmount) {
        subtotalAmount.textContent = new Intl.NumberFormat('vi-VN').format(total);
    }
}

function updateCartCountFromServer() {
    fetch('/cart/count', {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.json())
    .then(data => {
        const cartCountElements = document.querySelectorAll('.cart-count');
        cartCountElements.forEach(el => {
            el.textContent = data.count || 0;
        });
    })
    .catch(error => {
        // Silently fail for cart count updates
    });
}

function updateCartReviewContent() {
    const currentCartDropdown = document.getElementById('cart-dropdown');
    if (currentCartDropdown) {
        // Add updating class for animation
        currentCartDropdown.classList.add('updating');
    }
    
    fetch('/cart/review', {
        method: 'GET',
        headers: {
            'X-Requested-With': 'XMLHttpRequest'
        }
    })
    .then(response => response.text())
    .then(html => {
        // Parse the HTML response
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const newCartContent = doc.querySelector('#cart-dropdown');
        
        if (newCartContent && currentCartDropdown) {
            // Preserve the 'show' class if it exists
            const isCurrentlyShown = currentCartDropdown.classList.contains('show');
            
            // Replace the content with animation
            setTimeout(() => {
                currentCartDropdown.innerHTML = newCartContent.innerHTML;
                
                // Restore the 'show' class if needed
                if (isCurrentlyShown) {
                    currentCartDropdown.classList.add('show');
                }
                
                // Remove updating class
                currentCartDropdown.classList.remove('updating');
                
                // Add entering animation to new cart items
                const cartItems = currentCartDropdown.querySelectorAll('.cart-item');
                cartItems.forEach((item, index) => {
                    setTimeout(() => {
                        item.classList.add('entering');
                        setTimeout(() => {
                            item.classList.remove('entering');
                        }, 400);
                    }, index * 100);
                });
                
                // Re-initialize cart review functionality for new elements
                initializeCartReviewEvents();
            }, 150);
        }
    })
    .catch(error => {
        if (currentCartDropdown) {
            currentCartDropdown.classList.remove('updating');
        }
    });
}

function initializeCartReviewEvents() {
    const cartDropdown = document.getElementById('cart-dropdown');
    if (!cartDropdown) return;
    
    // Handle close button
    const cartClose = cartDropdown.querySelector('.cart-close');
    if (cartClose) {
        cartClose.addEventListener('click', function() {
            hideCartReview();
        });
    }
    
    // Prevent cart content clicks from closing the review
    const cartContent = cartDropdown.querySelector('.cart-content-wrap');
    if (cartContent) {
        cartContent.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    }
    
    // Handle remove product buttons
    const removeButtons = cartDropdown.querySelectorAll('.close-btn');
    removeButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const productId = this.getAttribute('data-product-id');
            if (productId) {
                removeFromCartReview(productId, this);
            }
        });
    });
} 