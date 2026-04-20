/**
 * Shopping page enhancements for sorting and filtering
 * Works in conjunction with pagination.js
 */
class ShoppingEnhancements {
    constructor() {
        this.init();
    }
    
    init() {
        this.setupSortingHandler();
        this.setupFilterHandler();
    }
    
    setupSortingHandler() {
        const sortSelect = document.querySelector('.single-select');
        if (sortSelect) {
            sortSelect.addEventListener('change', (e) => {
                this.sortProducts(e.target.value);
            });
        } else {
            // Try alternative selectors
            const altSelectors = ['select', '.form-select', '.category-select select', '.single-select'];
            for (let selector of altSelectors) {
                const element = document.querySelector(selector);
                if (element) {
                    element.addEventListener('change', (e) => {
                        this.sortProducts(e.target.value);
                    });
                    break;
                }
            }
        }
    }
    
    sortProducts(sortType) {
        const productsContainer = document.querySelector('.row.row--15');
        if (!productsContainer) return;
        
        // Get all visible products (not hidden by filters)
        const products = Array.from(productsContainer.querySelectorAll('.col-xl-4.col-sm-6'));
        
        if (products.length === 0) return;
        
        let sortedProducts = [...products]; // Create a copy to avoid mutation
        
        switch (sortType) {
            case 'name-asc':
            case 'Sort by Name A-Z':
            case 'Sort by name A-Z':
                sortedProducts.sort((a, b) => {
                    try {
                        const nameA = a.querySelector('.title a')?.textContent?.trim().toLowerCase() || '';
                        const nameB = b.querySelector('.title a')?.textContent?.trim().toLowerCase() || '';
                        return nameA.localeCompare(nameB, 'vi', { sensitivity: 'base' });
                    } catch (error) {
                        return 0;
                    }
                });
                break;
                
            case 'name-desc':
            case 'Sort by Name Z-A':
            case 'Sort by name Z-A':
                sortedProducts.sort((a, b) => {
                    try {
                        const nameA = a.querySelector('.title a')?.textContent?.trim().toLowerCase() || '';
                        const nameB = b.querySelector('.title a')?.textContent?.trim().toLowerCase() || '';
                        return nameB.localeCompare(nameA, 'vi', { sensitivity: 'base' });
                    } catch (error) {
                        return 0;
                    }
                });
                break;
                
            case 'price-asc':
            case 'Sort by Price: Low to High':
            case 'Sort by price ascending':
                sortedProducts.sort((a, b) => {
                    try {
                        const priceA = this.extractPrice(a.querySelector('.current-price')?.textContent || '0');
                        const priceB = this.extractPrice(b.querySelector('.current-price')?.textContent || '0');
                        return priceA - priceB;
                    } catch (error) {
                        return 0;
                    }
                });
                break;
                
            case 'price-desc':
            case 'Sort by Price: High to Low':
            case 'Sort by price descending':
                sortedProducts.sort((a, b) => {
                    try {
                        const priceA = this.extractPrice(a.querySelector('.current-price')?.textContent || '0');
                        const priceB = this.extractPrice(b.querySelector('.current-price')?.textContent || '0');
                        return priceB - priceA;
                    } catch (error) {
                        return 0;
                    }
                });
                break;
                
            case 'Sort by oldest':
                // Reverse the current order
                sortedProducts.reverse();
                break;
                
            default: // 'Sắp xếp theo mới nhất'
                // Keep original order - no sorting needed
                break;
        }
        
        // Clear container and re-append sorted products
        const fragment = document.createDocumentFragment();
        sortedProducts.forEach(product => {
            fragment.appendChild(product);
        });
        
        productsContainer.innerHTML = '';
        productsContainer.appendChild(fragment);
        
        // Add a small delay before refreshing pagination to ensure DOM is updated
        setTimeout(() => {
            if (window.productPagination) {
                window.productPagination.refresh();
            }
        }, 100);
    }
    
    extractPrice(priceText) {
        // Extract numeric value from price text (e.g., "1,000,000 VNĐ" -> 1000000)
        if (!priceText) return 0;
        
        // Remove all non-digit characters and convert to number
        const numericValue = priceText.replace(/[^\d]/g, '');
        return parseInt(numericValue) || 0;
    }
    
    setupFilterHandler() {
        // Handle mobile filter toggle
        const filterToggle = document.querySelector('.filter-toggle');
        if (filterToggle) {
            filterToggle.addEventListener('click', () => {
                const sidebar = document.querySelector('.axil-shop-sidebar');
                if (sidebar) {
                    sidebar.classList.toggle('show-mobile');
                }
            });
        }
        
        // Handle category filters (if sidebar has filter options)
        const categoryFilters = document.querySelectorAll('.category-filter');
        categoryFilters.forEach(filter => {
            filter.addEventListener('change', () => {
                this.filterProducts();
            });
        });
    }
    
    filterProducts() {
        // This method can be expanded to handle category filtering
        // For now, it just refreshes pagination
        if (window.productPagination) {
            window.productPagination.refresh();
        }
    }
}

// Initialize enhancements when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    setTimeout(() => {
        window.shoppingEnhancements = new ShoppingEnhancements();
    }, 200);
});

// Export for global access
window.ShoppingEnhancements = ShoppingEnhancements;

    document.addEventListener('DOMContentLoaded', function() {
    const searchForm = document.getElementById('header-search-form') || document.querySelector('form[action*="shopping-page"]');
    const searchInput = document.querySelector('input[name="search"]');

    if (searchForm && searchInput) {
    // Handle Enter key
    searchInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
    e.preventDefault();
    if (this.value.trim()) {
    window.location.href = '/user/shopping-page?search=' + encodeURIComponent(this.value.trim());
} else {
    this.focus();
}
}
});

    // Handle form submit
    searchForm.addEventListener('submit', function(e) {
    e.preventDefault();
    if (searchInput.value.trim()) {
    window.location.href = '/user/shopping-page?search=' + encodeURIComponent(searchInput.value.trim());
} else {
    searchInput.focus();
}
});

    // Handle button click
    const searchButton = searchForm.querySelector('.wooc-btn-search');
    if (searchButton) {
    searchButton.addEventListener('click', function(e) {
    e.preventDefault();
    if (searchInput.value.trim()) {
    window.location.href = '/user/shopping-page?search=' + encodeURIComponent(searchInput.value.trim());
} else {
    searchInput.focus();
}
});
}
}
});
