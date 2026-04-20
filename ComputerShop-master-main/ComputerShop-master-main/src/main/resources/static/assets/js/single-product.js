
    document.addEventListener('DOMContentLoaded', function() {
    const variantOptions = document.querySelectorAll('input[name="selectedVariant"]');
    const priceDisplay = document.getElementById('price-display');
    const stockDisplay = document.getElementById('variant-stock-display');
    const stockInfo = document.getElementById('stock-info');
    const addToCartButton = document.getElementById('add-to-cart-btn');
    const quantityInput = document.getElementById('quantity-input');
    const mainImageLink = document.getElementById('main-image-link');
    const zoomLink = document.getElementById('zoom-link');
    const thumbnailContainer = document.getElementById('thumbnail-container');
    const mainProductImage = document.getElementById('main-product-image');

    // Store original specifications
    const originalSpecs = {
    cpu: /*[[${specifications != null ? specifications.cpu : ''}]]*/ '',
    ram: /*[[${specifications != null ? specifications.ram : ''}]]*/ '',
    storage: /*[[${specifications != null ? specifications.ssd : ''}]]*/ '',
    gpu: /*[[${specifications != null ? specifications.vga : ''}]]*/ ''
};

    // Function to format number as currency
    function formatNumber(num) {
    return new Intl.NumberFormat('vi-VN').format(num);
}

    // Function to update display based on selected variant
    function updateVariantDisplay() {
    const selectedVariant = document.querySelector('input[name="selectedVariant"]:checked');
    if (selectedVariant) {
    const variantElement = selectedVariant.closest('.variant-card');
    const price = variantElement.dataset.price;
    const quantity = parseInt(variantElement.dataset.quantity);
    const variantId = variantElement.dataset.variantId;

    // Get variant specifications
    const variantSpecs = {
    cpu: variantElement.dataset.cpu,
    ram: variantElement.dataset.ram,
    storage: variantElement.dataset.storage,
    gpu: variantElement.dataset.gpu
};

    // Update price display
    if (priceDisplay) {
    priceDisplay.innerHTML = formatNumber(price) + ' VNĐ';
}

    // Update stock display
    if (stockDisplay && stockInfo) {
    if (quantity > 0) {
    stockDisplay.textContent = quantity;
    stockInfo.innerHTML = '<i class="fal fa-check"></i>In Stock: <span id="variant-stock-display">' + quantity + '</span> products';
    stockInfo.style.color = '';
} else {
    stockDisplay.textContent = '0';
    stockInfo.innerHTML = '<i class="fal fa-times"></i>Out of stock';
    stockInfo.style.color = '#ff4757';
}
}

    // Update specifications table
    updateSpecificationTable(variantSpecs);

    // Update add to cart button
    if (quantity > 0) {
    addToCartButton.style.display = 'inline-block';
    addToCartButton.classList.remove('disabled');
    addToCartButton.dataset.variantId = variantId;
} else {
    addToCartButton.style.display = 'none';
    addToCartButton.classList.add('disabled');
}

    // Update main product image when variant is selected
    const selectedImageUrl = variantElement.dataset.image;
    console.log('Selected image URL:', selectedImageUrl);
    
    if (selectedImageUrl && selectedImageUrl !== 'null' && selectedImageUrl !== '' && selectedImageUrl.trim() !== '') {
        const fullImagePath = `/assets/images/product/laptop/${selectedImageUrl}`;
        console.log('Full image path:', fullImagePath);
        
        if (mainProductImage) {
            // Check if image exists before setting
            const testImg = new Image();
            testImg.onload = function() {
                mainProductImage.src = fullImagePath;
                console.log('Image loaded successfully:', fullImagePath);
            };
            testImg.onerror = function() {
                console.warn('Image failed to load:', fullImagePath);
                // Fallback to default product image if variant image fails
                const defaultImagePath = `/assets/images/product/laptop/${/*[[${product.imageURL}]]*/ ''}`;
                mainProductImage.src = defaultImagePath;
            };
            testImg.src = fullImagePath;
        }
        
        if (mainImageLink) {
            mainImageLink.href = fullImagePath;
        }
        if (zoomLink) {
            zoomLink.href = fullImagePath;
        }

        // Update active thumbnail
        if (thumbnailContainer) {
            const activeThumb = thumbnailContainer.querySelector('.small-thumb-img.active');
            if (activeThumb) {
                activeThumb.classList.remove('active');
            }
            const newActiveThumb = thumbnailContainer.querySelector(`[data-image="${selectedImageUrl}"]`);
            if (newActiveThumb) {
                newActiveThumb.classList.add('active');
            } else {
                // If no matching thumbnail, activate the first one (main product image)
                const firstThumb = thumbnailContainer.querySelector('.small-thumb-img[data-variant-id="main"]');
                if (firstThumb) {
                    firstThumb.classList.add('active');
                }
            }
        }
    }
}
}

    // Function to update specification table
    function updateSpecificationTable(variantSpecs) {
    // Update CPU
    const cpuElement = document.getElementById('spec-cpu');
    if (cpuElement) {
    cpuElement.textContent = variantSpecs.cpu && variantSpecs.cpu !== 'null' ? variantSpecs.cpu : originalSpecs.cpu;
}

    // Update RAM
    const ramElement = document.getElementById('spec-ram');
    if (ramElement) {
    ramElement.textContent = variantSpecs.ram && variantSpecs.ram !== 'null' ? variantSpecs.ram : originalSpecs.ram;
}

    // Update Storage
    const storageElement = document.getElementById('spec-storage');
    if (storageElement) {
    storageElement.textContent = variantSpecs.storage && variantSpecs.storage !== 'null' ? variantSpecs.storage : originalSpecs.storage;
}

    // Update GPU
    const gpuElement = document.getElementById('spec-gpu');
    if (gpuElement) {
    gpuElement.textContent = variantSpecs.gpu && variantSpecs.gpu !== 'null' ? variantSpecs.gpu : originalSpecs.gpu;
}
}

    // Add event listeners to variant options
    variantOptions.forEach(option => {
    option.addEventListener('change', updateVariantDisplay);
});

    // Initialize display
    updateVariantDisplay();

    // Handle thumbnail clicks
    if (thumbnailContainer) {
        thumbnailContainer.addEventListener('click', function(e) {
            const clickedThumb = e.target.closest('.small-thumb-img');
            if (clickedThumb) {
                const imageUrl = clickedThumb.dataset.image;
                const variantId = clickedThumb.dataset.variantId;
                
                console.log('Thumbnail clicked:', imageUrl, 'Variant ID:', variantId);

                // Update main image
                if (mainProductImage && imageUrl) {
                    const fullImagePath = `/assets/images/product/laptop/${imageUrl}`;
                    
                    // Check if image exists before setting
                    const testImg = new Image();
                    testImg.onload = function() {
                        mainProductImage.src = fullImagePath;
                        mainProductImage.alt = `Product image - ${imageUrl}`;
                        console.log('Thumbnail image loaded successfully:', fullImagePath);

                        // Update zoom links
                        if (mainImageLink) {
                            mainImageLink.href = fullImagePath;
                        }
                        if (zoomLink) {
                            zoomLink.href = fullImagePath;
                        }
                    };
                    testImg.onerror = function() {
                        console.warn('Thumbnail image failed to load:', fullImagePath);
                    };
                    testImg.src = fullImagePath;
                }

                // Update active thumbnail
                const activeThumb = thumbnailContainer.querySelector('.small-thumb-img.active');
                if (activeThumb) {
                    activeThumb.classList.remove('active');
                }
                clickedThumb.classList.add('active');

                // If clicking on variant image, auto-select that variant
                if (variantId && variantId !== 'main') {
                    const variantRadio = document.querySelector(`input[name="selectedVariant"][value="${variantId}"]`);
                    if (variantRadio && !variantRadio.disabled) {
                        variantRadio.checked = true;
                        updateVariantDisplay();
                        console.log('Auto-selected variant:', variantId);
                    }
                }
            }
        });
    }

    // Handle add to cart
    if (addToCartButton) {
    addToCartButton.addEventListener('click', function(e) {
    e.preventDefault();

    const productId = this.dataset.productId;
    const variantId = this.dataset.variantId;
    const quantity = parseInt(quantityInput.value) || 1;

    // If product has variants
    if (variantId) {
    fetch('/cart/add-variant', {
    method: 'POST',
    headers: {
    'Content-Type': 'application/x-www-form-urlencoded',
    'X-Requested-With': 'XMLHttpRequest'
},
    body: `productId=${productId}&variantId=${variantId}&quantity=${quantity}`
})
    .then(response => response.json())
                .then(data => {
                if (data.success) {
                    // Show success notification
                    alert(data.message);
                    
                    // Update cart count if available
                    if (data.cartCount !== undefined) {
                        const cartCountElement = document.querySelector('.cart-count');
                        if (cartCountElement) {
                            cartCountElement.textContent = data.cartCount;
                        }
                    }
                    
                    // Update cart review dropdown using shared function from cart-animation.js
                    if (typeof updateCartReviewContent === 'function') {
                        updateCartReviewContent();
                    }
                    
                    // Show cart dropdown briefly using shared function
                    setTimeout(() => {
                        if (typeof showCartReview === 'function') {
                            showCartReview();
                            // Hide after 3 seconds
                            setTimeout(() => {
                                if (typeof hideCartReview === 'function') {
                                    hideCartReview();
                                }
                            }, 3000);
                        }
                    }, 300);
                } else {
                    alert(data.message || 'An error occurred');
                }
            })
    .catch(error => {

    alert('An error occurred while adding to the cart!');
});
} else {
    // No variants, add product directly
    window.location.href = `/cart/add/${productId}?sl=${quantity}`;
}
});
}
});

    // === REVIEW FUNCTIONALITY ===
    const addToCartBtn = document.getElementById('add-to-cart-btn');
    const productId = addToCartBtn ? addToCartBtn.getAttribute('data-product-id') : '';
    console.log('Product ID from data attribute:', productId);
    let currentPage = 0;
    const pageSize = 10;

    // Load reviews when tab is clicked
    document.getElementById('reviews-tab').addEventListener('click', function() {
        loadReviews();
    });

    // Auto-load reviews when page loads
    if (productId && productId.trim() !== '') {
        loadReviews();
    }

    // Load more reviews button
    const loadMoreBtn = document.getElementById('load-more-reviews');
    if (loadMoreBtn) {
    loadMoreBtn.addEventListener('click', function() {
        currentPage++;
        loadReviews(false); // append to existing reviews
    });
}

    function loadReviews(clearExisting = true) {
        if (!productId || productId.trim() === '') {
            console.error('ProductId is empty, cannot load reviews');
            return;
        }

        const url = `/api/reviews/product/${productId}?page=${currentPage}&size=${pageSize}`;
        console.log('Loading reviews from:', url);

        fetch(url)
            .then(response => {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Reviews data received:', data);
                const reviewsList = document.getElementById('reviews-list');

                if (clearExisting) {
                    reviewsList.innerHTML = '';
                    currentPage = 0;
                }

                if (data.content && data.content.length > 0) {
                    data.content.forEach(review => {
                        reviewsList.appendChild(createReviewElement(review));
                    });

                    // Show/hide load more button
                    const loadMoreBtn = document.getElementById('load-more-reviews');
                    if (data.last) {
                        loadMoreBtn.style.display = 'none';
                    } else {
                        loadMoreBtn.style.display = 'block';
                    }
                } else if (clearExisting) {
                    console.log('No reviews found');
                    reviewsList.innerHTML = '<li class="text-center py-4"><p>There are no reviews for this product yet.</p></li>';
                }
            })
            .catch(error => {
                console.error('Error loading reviews:', error);
                document.getElementById('reviews-list').innerHTML =
                    `<li class="text-center py-4"><p class="text-danger">There was an error loading reviews: ${error.message}</p></li>`;
            });
    }

    function createReviewElement(review) {
    const reviewLi = document.createElement('li');
    reviewLi.className = 'comment';

    // Generate stars HTML based on rating
    let starsHtml = '';
    for (let i = 1; i <= 5; i++) {
    if (i <= review.rating) {
    starsHtml += '<a href="#"><i class="fas fa-star"></i></a>';
} else {
    starsHtml += '<a href="#"><i class="far fa-star"></i></a>';
}
}

    // Format date
    const date = new Date(review.createdAt).toLocaleDateString('vi-VN');

    // Use the template structure provided
    reviewLi.innerHTML = `
            <div class="comment-body">
                <div class="single-comment">
                    <div class="comment-img">
                        <img src="/assets/images/product/author.jpg" alt="Author Images">
                    </div>
                    <div class="comment-inner">
                        <h6 class="commenter">
                            <a class="hover-flip-item-wrapper" href="#">
                                <span class="hover-flip-item">
                                    <span data-text="${review.user?.fullName || 'Anonymous User'}">${review.user?.fullName || 'Anonymous User'}</span>
                                </span>
                            </a>
                            <span class="commenter-rating">
                                ${starsHtml}
                            </span>
                        </h6>
                        <div class="comment-text">
                            <p>"${review.comment || ''}"</p>
                        </div>
                        <div class="comment-date text-muted small">
                            ${date}
                        </div>
                    </div>
                </div>
            </div>
        `;

    return reviewLi;
}

