/**
 * Common Utilities for ComputerShop
 * Reusable functions across multiple pages
 */

// Notification utilities
const NotificationUtils = {
    // Show success notification
    showSuccess: function(message, duration = 5000) {
        this.createNotification('success', message, duration);
    },
    
    // Show error notification
    showError: function(message, duration = 7000) {
        this.createNotification('error', message, duration);
    },
    
    // Show info notification
    showInfo: function(message, duration = 5000) {
        this.createNotification('info', message, duration);
    },
    
    // Show warning notification
    showWarning: function(message, duration = 6000) {
        this.createNotification('warning', message, duration);
    },
    
    // Create notification element
    createNotification: function(type, message, duration) {
        const alertDiv = document.createElement('div');
        const typeConfig = this.getTypeConfig(type);
        
        alertDiv.className = `alert alert-${typeConfig.class} alert-dismissible fade show position-fixed`;
        alertDiv.style.cssText = 'top: 20px; right: 20px; z-index: 9999; max-width: 400px;';
        alertDiv.innerHTML = `
            <i class="${typeConfig.icon}"></i>
            <strong>${typeConfig.title}</strong> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        document.body.appendChild(alertDiv);
        
        // Auto remove after specified duration
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, duration);
        
        return alertDiv;
    },
    
    // Get configuration for notification types
    getTypeConfig: function(type) {
        const configs = {
            success: {
                class: 'success',
                icon: 'fas fa-check-circle',
                title: 'Success!'
            },
            error: {
                class: 'danger',
                icon: 'fas fa-exclamation-triangle',
                title: 'Error!'
            },
            info: {
                class: 'info',
                icon: 'fas fa-info-circle',
                title: 'Info!'
            },
            warning: {
                class: 'warning',
                icon: 'fas fa-exclamation-circle',
                title: 'Warning!'
            }
        };
        
        return configs[type] || configs.info;
    }
};

// Cart utilities
const CartUtils = {
    // Update cart count in UI
    updateCartCount: function(count) {
        const cartCountElements = document.querySelectorAll('.cart-count');
        cartCountElements.forEach(element => {
            element.textContent = count;
            
            // Add animation effect
            element.style.transform = 'scale(1.2)';
            setTimeout(() => {
                element.style.transform = 'scale(1)';
            }, 200);
        });
    },
    
    // Get current cart count via AJAX
    getCurrentCartCount: function(callback) {
        fetch('/cart/count', {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (callback && typeof callback === 'function') {
                callback(data.count || 0);
            }
        })
        .catch(error => {
            // Silently fail for cart count updates
            if (callback && typeof callback === 'function') {
                callback(0);
            }
        });
    }
};

// Loading utilities
const LoadingUtils = {
    // Show loading state on button
    showButtonLoading: function(button, loadingText = 'Processing...') {
        if (!button.dataset.originalText) {
            button.dataset.originalText = button.innerHTML;
        }
        button.classList.add('btn-loading');
        button.innerHTML = loadingText;
        button.disabled = true;
    },
    
    // Hide loading state on button
    hideButtonLoading: function(button) {
        button.classList.remove('btn-loading');
        if (button.dataset.originalText) {
            button.innerHTML = button.dataset.originalText;
        }
        button.disabled = false;
    }
};

// Modal utilities
const ModalUtils = {
    // Show Bootstrap modal
    show: function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            const bsModal = new bootstrap.Modal(modal);
            bsModal.show();
            return bsModal;
        }
        return null;
    },
    
    // Hide Bootstrap modal
    hide: function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            const bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) {
                bsModal.hide();
            }
        }
    },
    
    // Hide all open modals
    hideAll: function() {
        const modals = document.querySelectorAll('.modal.show');
        modals.forEach(modal => {
            const bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) {
                bsModal.hide();
            }
        });
    }
};

// Form utilities
const FormUtils = {
    // Serialize form data to object
    serializeToObject: function(form) {
        const formData = new FormData(form);
        const object = {};
        formData.forEach((value, key) => {
            object[key] = value;
        });
        return object;
    },
    
    // Reset form with animation
    resetForm: function(form) {
        form.reset();
        // Add visual feedback
        form.style.opacity = '0.5';
        setTimeout(() => {
            form.style.opacity = '1';
        }, 200);
    },
    
    // Validate required fields
    validateRequired: function(form) {
        const requiredFields = form.querySelectorAll('[required]');
        let isValid = true;
        
        requiredFields.forEach(field => {
            if (!field.value.trim()) {
                field.classList.add('is-invalid');
                isValid = false;
            } else {
                field.classList.remove('is-invalid');
            }
        });
        
        return isValid;
    }
};

// Animation utilities
const AnimationUtils = {
    // Fade in element
    fadeIn: function(element, duration = 300) {
        element.style.opacity = '0';
        element.style.display = 'block';
        
        let opacity = 0;
        const timer = setInterval(() => {
            opacity += 50 / duration;
            if (opacity >= 1) {
                clearInterval(timer);
                opacity = 1;
            }
            element.style.opacity = opacity;
        }, 50);
    },
    
    // Fade out element
    fadeOut: function(element, duration = 300) {
        let opacity = 1;
        const timer = setInterval(() => {
            opacity -= 50 / duration;
            if (opacity <= 0) {
                clearInterval(timer);
                opacity = 0;
                element.style.display = 'none';
            }
            element.style.opacity = opacity;
        }, 50);
    },
    
    // Slide up element
    slideUp: function(element, duration = 300) {
        element.style.transitionProperty = 'height, margin, padding';
        element.style.transitionDuration = duration + 'ms';
        element.style.boxSizing = 'border-box';
        element.style.height = element.offsetHeight + 'px';
        element.offsetHeight; // Force reflow
        element.style.overflow = 'hidden';
        element.style.height = 0;
        element.style.paddingTop = 0;
        element.style.paddingBottom = 0;
        element.style.marginTop = 0;
        element.style.marginBottom = 0;
        
        setTimeout(() => {
            element.style.display = 'none';
            element.style.removeProperty('height');
            element.style.removeProperty('padding-top');
            element.style.removeProperty('padding-bottom');
            element.style.removeProperty('margin-top');
            element.style.removeProperty('margin-bottom');
            element.style.removeProperty('overflow');
            element.style.removeProperty('transition-duration');
            element.style.removeProperty('transition-property');
        }, duration);
    }
};

// Device utilities
const DeviceUtils = {
    // Check if mobile device
    isMobile: function() {
        return window.innerWidth <= 768;
    },
    
    // Check if tablet device
    isTablet: function() {
        return window.innerWidth > 768 && window.innerWidth <= 1024;
    },
    
    // Check if desktop device
    isDesktop: function() {
        return window.innerWidth > 1024;
    },
    
    // Check if touch device
    isTouchDevice: function() {
        return 'ontouchstart' in window || navigator.maxTouchPoints > 0;
    }
};

// Export utilities to global scope
window.ComputerShop = {
    Notification: NotificationUtils,
    Cart: CartUtils,
    Loading: LoadingUtils,
    Modal: ModalUtils,
    Form: FormUtils,
    Animation: AnimationUtils,
    Device: DeviceUtils
};

// Đảm bảo hàm toggleReportsMenu luôn tồn tại trên mọi trang
window.toggleReportsMenu = function(event) {
    const menu = document.getElementById('collapseReports');
    const link = (event && event.target) ? event.target.closest('.nav-link') : document.querySelector('.nav-link[onclick*="toggleReportsMenu"]');
    if (!menu || !link) return;
    if (menu.classList.contains('show')) {
        menu.classList.remove('show');
        link.classList.add('collapsed');
    } else {
        document.querySelectorAll('.sidebar .collapse.show').forEach(function(openMenu) {
            openMenu.classList.remove('show');
        });
        document.querySelectorAll('.sidebar .nav-link').forEach(function(navLink) {
            navLink.classList.add('collapsed');
        });
        menu.classList.add('show');
        link.classList.remove('collapsed');
    }
};

// Initialize common features
document.addEventListener('DOMContentLoaded', function() {
    // Add smooth scrolling to all anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Add keyboard navigation support
    document.addEventListener('keydown', function(e) {
        // Close modals with Escape key
        if (e.key === 'Escape') {
            ModalUtils.hideAll();
        }
    });
    
    // Initialize tooltips if Bootstrap is available
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }
    
    // Initialize popovers if Bootstrap is available
    if (typeof bootstrap !== 'undefined' && bootstrap.Popover) {
        const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });
    }
}); 