// Homepage Slider Initialization
$(document).ready(function() {
    // Wait for complete DOM and CSS loading
    setTimeout(function() {
        initializeHomepageSliders();
    }, 300);
});

function initializeHomepageSliders() {
    console.log('Initializing homepage sliders...');
    
    // Helper function for safe slider initialization
    function safeSlickInit(selector, options) {
        try {
            var $element = $(selector);
            if ($element.length > 0 && !$element.hasClass('slick-initialized')) {
                console.log('Initializing slider for:', selector);
                $element.slick(options);
                console.log('Successfully initialized:', selector);
            }
        } catch (error) {
            console.warn('Slick initialization failed for ' + selector + ':', error);
        }
    }

    // Only initialize sliders that exist on homepage
    
    // Brand carousel (categories)
    safeSlickInit('.categrie-product-activation', {
        infinite: true,
        slidesToShow: 5,
        slidesToScroll: 1,
        arrows: true,
        dots: false,
        autoplay: false,
        speed: 1000,
        prevArrow: '<button class="prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
        nextArrow: '<button class="next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
        responsive: [
            {breakpoint: 1200, settings: {slidesToShow: 4}},
            {breakpoint: 992, settings: {slidesToShow: 3}},
            {breakpoint: 768, settings: {slidesToShow: 2}},
            {breakpoint: 480, settings: {slidesToShow: 1}}
        ]
    });

    // New arrivals products
    safeSlickInit('.new-arrivals-product-activation', {
        infinite: true,
        slidesToShow: 4,
        slidesToScroll: 1,
        arrows: true,
        dots: false,
        prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
        nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
        responsive: [
            {breakpoint: 1199, settings: {slidesToShow: 3}},
            {breakpoint: 991, settings: {slidesToShow: 2}},
            {breakpoint: 576, settings: {slidesToShow: 1}}
        ]
    });

    // Testimonials
    safeSlickInit('.testimonial-slick-activation', {
        infinite: true,
        slidesToShow: 3,
        slidesToScroll: 1,
        arrows: true,
        dots: false,
        speed: 500,
        draggable: true,
        prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
        nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
        responsive: [
            {breakpoint: 991, settings: {slidesToShow: 1}}
        ]
    });

    console.log('Homepage sliders initialization completed');
} 