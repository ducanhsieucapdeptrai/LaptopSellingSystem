/**
 * Client-side pagination for shopping page
 * Displays 21 products per page with navigation controls
 */
class ProductPagination {
    constructor(containerSelector, itemsPerPage = 21) {
        this.container = document.querySelector(containerSelector);
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 1;
        this.products = [];
        this.totalPages = 0;
        
        this.init();
    }
    
    init() {
        if (!this.container) return;
        
        // Get all product items
        this.products = Array.from(this.container.querySelectorAll('.col-xl-4.col-sm-6'));
        this.totalPages = Math.ceil(this.products.length / this.itemsPerPage);
        
        if (this.totalPages <= 1) return; // No pagination needed
        
        this.hideAllProducts();
        this.showCurrentPage();
        this.createPaginationControls();
        this.updateProductCount();
    }
    
    hideAllProducts() {
        this.products.forEach(product => {
            product.style.display = 'none';
            product.classList.remove('product-fade-in');
        });
    }
    
    showCurrentPage() {
        const startIndex = (this.currentPage - 1) * this.itemsPerPage;
        const endIndex = startIndex + this.itemsPerPage;
        
        this.products.slice(startIndex, endIndex).forEach((product, index) => {
            product.style.display = 'block';
            // Add fade-in animation
            setTimeout(() => {
                product.classList.add('product-fade-in');
            }, index * 50); // Stagger the animation
        });
    }
    
    createPaginationControls() {
        // Remove existing pagination if any
        const existingPagination = document.querySelector('.pagination-wrapper');
        if (existingPagination) {
            existingPagination.remove();
        }
        
        const paginationWrapper = document.createElement('div');
        paginationWrapper.className = 'pagination-wrapper mt-4';
        paginationWrapper.innerHTML = this.getPaginationHTML();
        
        // Insert pagination after the product grid
        this.container.parentNode.insertBefore(paginationWrapper, this.container.nextSibling);
        
        // Add event listeners
        this.addPaginationEventListeners(paginationWrapper);
    }
    
    getPaginationHTML() {
        let html = '<nav aria-label="Product pagination"><ul class="pagination justify-content-center">';
        
        // Previous button
        html += `<li class="page-item ${this.currentPage === 1 ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="${this.currentPage - 1}" aria-label="Previous">
                        <span aria-hidden="true">&laquo;</span>
                    </a>
                 </li>`;
        
        // Page numbers
        const startPage = Math.max(1, this.currentPage - 2);
        const endPage = Math.min(this.totalPages, this.currentPage + 2);
        
        if (startPage > 1) {
            html += `<li class="page-item"><a class="page-link" href="#" data-page="1">1</a></li>`;
            if (startPage > 2) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
        }
        
        for (let i = startPage; i <= endPage; i++) {
            html += `<li class="page-item ${i === this.currentPage ? 'active' : ''}">
                        <a class="page-link" href="#" data-page="${i}">${i}</a>
                     </li>`;
        }
        
        if (endPage < this.totalPages) {
            if (endPage < this.totalPages - 1) {
                html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
            }
            html += `<li class="page-item"><a class="page-link" href="#" data-page="${this.totalPages}">${this.totalPages}</a></li>`;
        }
        
        // Next button
        html += `<li class="page-item ${this.currentPage === this.totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="#" data-page="${this.currentPage + 1}" aria-label="Next">
                        <span aria-hidden="true">&raquo;</span>
                    </a>
                 </li>`;
        
        html += '</ul></nav>';
        
        // Add page info
        const startItem = (this.currentPage - 1) * this.itemsPerPage + 1;
        const endItem = Math.min(this.currentPage * this.itemsPerPage, this.products.length);
        html += `<div class="pagination-info text-center mt-3">
                    <small class="text-muted">Displaying ${startItem}-${endItem} of ${this.products.length} products</small>
                 </div>`;
        
        return html;/**
                     * Client-side pagination for shopping page
                     * Displays 21 products per page with navigation controls
                     */
                    class ProductPagination {
                        constructor(containerSelector, itemsPerPage = 21) {
                            this.container = document.querySelector(containerSelector);
                            this.itemsPerPage = itemsPerPage;
                            this.currentPage = 1;
                            this.products = [];
                            this.totalPages = 0;

                            this.init();
                        }

                        init() {
                            if (!this.container) return;

                            // Get all product items
                            this.products = Array.from(this.container.querySelectorAll('.col-xl-4.col-sm-6'));
                            this.totalPages = Math.ceil(this.products.length / this.itemsPerPage);

                            if (this.totalPages <= 1) return; // No pagination needed

                            this.hideAllProducts();
                            this.showCurrentPage();
                            this.createPaginationControls();
                            this.updateProductCount();
                        }

                        hideAllProducts() {
                            this.products.forEach(product => {
                                product.style.display = 'none';
                                product.classList.remove('product-fade-in');
                            });
                        }

                        showCurrentPage() {
                            const startIndex = (this.currentPage - 1) * this.itemsPerPage;
                            const endIndex = startIndex + this.itemsPerPage;

                            this.products.slice(startIndex, endIndex).forEach((product, index) => {
                                product.style.display = 'block';
                                // Add fade-in animation
                                setTimeout(() => {
                                    product.classList.add('product-fade-in');
                                }, index * 50); // Stagger animation
                            });
                        }

                        createPaginationControls() {
                            // Remove existing pagination if any
                            const existingPagination = document.querySelector('.pagination-wrapper');
                            if (existingPagination) {
                                existingPagination.remove();
                            }

                            const paginationWrapper = document.createElement('div');
                            paginationWrapper.className = 'pagination-wrapper mt-4';
                            paginationWrapper.innerHTML = this.getPaginationHTML();

                            // Insert pagination after the product grid
                            this.container.parentNode.insertBefore(paginationWrapper, this.container.nextSibling);

                            // Add event listeners
                            this.addPaginationEventListeners(paginationWrapper);
                        }

                        getPaginationHTML() {
                            let html = '<nav aria-label="Product pagination"><ul class="pagination justify-content-center">';

                            // Previous button
                            html += `<li class="page-item ${this.currentPage === 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="#" data-page="${this.currentPage - 1}" aria-label="Previous">
                                            <span aria-hidden="true">&laquo;</span>
                                        </a>
                                     </li>`;

                            // Page numbers
                            const startPage = Math.max(1, this.currentPage - 2);
                            const endPage = Math.min(this.totalPages, this.currentPage + 2);

                            if (startPage > 1) {
                                html += `<li class="page-item"><a class="page-link" href="#" data-page="1">1</a></li>`;
                                if (startPage > 2) {
                                    html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
                                }
                            }

                            for (let i = startPage; i <= endPage; i++) {
                                html += `<li class="page-item ${i === this.currentPage ? 'active' : ''}">
                                            <a class="page-link" href="#" data-page="${i}">${i}</a>
                                         </li>`;
                            }

                            if (endPage < this.totalPages) {
                                if (endPage < this.totalPages - 1) {
                                    html += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
                                }
                                html += `<li class="page-item"><a class="page-link" href="#" data-page="${this.totalPages}">${this.totalPages}</a></li>`;
                            }

                            // Next button
                            html += `<li class="page-item ${this.currentPage === this.totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="#" data-page="${this.currentPage + 1}" aria-label="Next">
                                            <span aria-hidden="true">&raquo;</span>
                                        </a>
                                     </li>`;

                            html += '</ul></nav>';

                            // Page information
                            const startItem = (this.currentPage - 1) * this.itemsPerPage + 1;
                            const endItem = Math.min(this.currentPage * this.itemsPerPage, this.products.length);
                            html += `<div class="pagination-info text-center mt-3">
                                        <small class="text-muted">Displaying ${startItem}-${endItem} of ${this.products.length} products</small>
                                     </div>`;

                            return html;
                        }

                        addPaginationEventListeners(wrapper) {
                            const links = wrapper.querySelectorAll('.page-link[data-page]');
                            links.forEach(link => {
                                link.addEventListener('click', (e) => {
                                    e.preventDefault();
                                    const page = parseInt(link.getAttribute('data-page'));
                                    if (page && page !== this.currentPage && page >= 1 && page <= this.totalPages) {
                                        this.goToPage(page);
                                    }
                                });
                            });
                        }

                        goToPage(page) {
                            this.currentPage = page;
                            this.hideAllProducts();
                            this.showCurrentPage();
                            this.createPaginationControls();

                            // Scroll to top of product section
                            this.container.scrollIntoView({ behavior: 'smooth', block: 'start' });
                        }

                        updateProductCount() {
                            // Update product count display if it exists
                            const filterResults = document.querySelector('.filter-results');
                            if (filterResults) {
                                const startItem = (this.currentPage - 1) * this.itemsPerPage + 1;
                                const endItem = Math.min(this.currentPage * this.itemsPerPage, this.products.length);
                                filterResults.textContent = `Displaying ${startItem}-${endItem} of ${this.products.length} products`;
                            }
                        }

                        // Method to refresh pagination (useful when products are filtered)
                        refresh() {
                            // Get all product items
                            this.products = Array.from(this.container.querySelectorAll('.col-xl-4.col-sm-6'));
                            this.totalPages = Math.ceil(this.products.length / this.itemsPerPage);
                            this.currentPage = 1;

                            if (this.totalPages <= 1) {
                                const existingPagination = document.querySelector('.pagination-wrapper');
                                if (existingPagination) {
                                    existingPagination.remove();
                                }
                                // Show all products if pagination is not required
                                this.products.forEach(product => {
                                    product.style.display = 'block';
                                });
                                return;
                            }

                            this.hideAllProducts();
                            this.showCurrentPage();
                            this.createPaginationControls();
                            this.updateProductCount();
                        }
                    }

                    // Initialize pagination when DOM is loaded
                    document.addEventListener('DOMContentLoaded', function() {
                        // Wait briefly to ensure all content has loaded
                        setTimeout(() => {
                            window.productPagination = new ProductPagination('.row.row--15', 21);
                        }, 100);
                    });

                    // Export for global access
                    window.ProductPagination = ProductPagination;
    }
    
    addPaginationEventListeners(wrapper) {
        const links = wrapper.querySelectorAll('.page-link[data-page]');
        links.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(link.getAttribute('data-page'));
                if (page && page !== this.currentPage && page >= 1 && page <= this.totalPages) {
                    this.goToPage(page);
                }
            });
        });
    }
    
    goToPage(page) {
        this.currentPage = page;
        this.hideAllProducts();
        this.showCurrentPage();
        this.createPaginationControls();
        
        // Scroll to top of products section
        this.container.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
    
    updateProductCount() {
        // Update the product count display if it exists
        const filterResults = document.querySelector('.filter-results');
        if (filterResults) {
            const startItem = (this.currentPage - 1) * this.itemsPerPage + 1;
            const endItem = Math.min(this.currentPage * this.itemsPerPage, this.products.length);
            filterResults.textContent = `Displaying ${startItem}-${endItem} of ${this.products.length} products`;
        }
    }
    
    // Method to refresh pagination (useful when products are filtered)
    refresh() {
        // Get all product items (don't filter by display style since they might be sorted)
        this.products = Array.from(this.container.querySelectorAll('.col-xl-4.col-sm-6'));
        this.totalPages = Math.ceil(this.products.length / this.itemsPerPage);
        this.currentPage = 1;
        
        if (this.totalPages <= 1) {
            const existingPagination = document.querySelector('.pagination-wrapper');
            if (existingPagination) {
                existingPagination.remove();
            }
            // Show all products if no pagination needed
            this.products.forEach(product => {
                product.style.display = 'block';
            });
            return;
        }
        
        this.hideAllProducts();
        this.showCurrentPage();
        this.createPaginationControls();
        this.updateProductCount();
    }
}

// Initialize pagination when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Wait a bit to ensure all content is loaded
    setTimeout(() => {
        window.productPagination = new ProductPagination('.row.row--15', 21);
    }, 100);
});

// Export for global access
window.ProductPagination = ProductPagination; 