(function(window, document, $, undefined) {
    'use strict';

    var axilInit = {
        i: function(e) {
            axilInit.s();
            axilInit.methods();
        },

        s: function(e) {
            this._window = $(window),
                this._document = $(document),
                this._body = $('body'),
                this._html = $('html')
        },

        methods: function(e) {
            axilInit.w();
            axilInit.contactForm();
            axilInit.axilBackToTop();
            axilInit.shopFilterWidget();
            axilInit.mobileMenuActivation();
            axilInit.menuLinkActive();
            axilInit.headerIconToggle();
            axilInit.priceRangeSlider();
            axilInit.quantityRanger();
            axilInit.axilSlickActivation();
            axilInit.countdownInit('.coming-countdown', '2025/10/01');
            axilInit.campaignCountdown('.campaign-countdown', '2025/10/01');
            axilInit.countdownInit('.poster-countdown', '2025/06/01');
            axilInit.countdownInit('.sale-countdown', '2025/10/31');
            axilInit.sideOffcanvasToggle('.cart-dropdown-btn', '#cart-dropdown');
            axilInit.sideOffcanvasToggle('.mobile-nav-toggler', '.header-main-nav');
            axilInit.sideOffcanvasToggle('.department-side-menu', '.department-nav-menu');
            axilInit.sideOffcanvasToggle('.filter-toggle', '.axil-shop-sidebar');
            axilInit.sideOffcanvasToggle('.axil-search', '#header-search-modal');
            axilInit.sideOffcanvasToggle('.popup-close, .closeMask', "#offer-popup-modal");
            axilInit.stickyHeaderMenu();
            axilInit.salActivation();
            axilInit.magnificPopupActivation();
            axilInit.colorVariantActive();
            axilInit.headerCampaignRemove();
            axilInit.axilMasonary();
            axilInit.counterUpActivation();
            axilInit.scrollSmoth();
            axilInit.onLoadClassAdd();
            axilInit.dropdownMenuSlide();
           
           
        },

        w: function(e) {
            this._window.on('load', axilInit.l).on('scroll', axilInit.res)
        },

        contactForm: function() {
            $('.axil-contact-form').on('submit', function(e) {
                e.preventDefault();
                var _self = $(this);
                var _selector = _self.closest('input,textarea');
                _self.closest('div').find('input,textarea').removeAttr('style');
                _self.find('.error-msg').remove();
                _self.closest('div').find('button[type="submit"]').attr('disabled', 'disabled');
                var data = $(this).serialize();
                $.ajax({
                    url: 'mail.php',
                    type: "post",
                    dataType: 'json',
                    data: data,
                    success: function(data) {
                        _self.closest('div').find('button[type="submit"]').removeAttr('disabled');
                        if (data.code == false) {
                            _self.closest('div').find('[name="' + data.field + '"]');
                            _self.find('.axil-btn').after('<div class="error-msg"><p>*' + data.err + '</p></div>');
                        } else {
                            $('.error-msg').hide();
                            $('.form-group').removeClass('focused');
                            _self.find('.axil-btn').after('<div class="success-msg"><p>' + data.success + '</p></div>');
                            _self.closest('div').find('input,textarea').val('');

                            setTimeout(function() {
                                $('.success-msg').fadeOut('slow');
                            }, 5000);
                        }
                    }
                });
            });
        },

        counterUpActivation: function () {
			var _counter = $('.count');
			if (_counter.length) {
				_counter.counterUp({
					delay: 10,
					time: 1000,
					triggerOnce: true
				});
			}
        },

        scrollSmoth: function (e) {
            $(document).on('click', '.smoth-animation', function (event) {
                event.preventDefault();
                $('html, body').animate({
                    scrollTop: $($.attr(this, 'href')).offset().top
                }, 200);
            });
        },

        axilBackToTop: function() {
            var btn = $('#backto-top');
            $(window).scroll(function() {
                if ($(window).scrollTop() > 300) {
                    btn.addClass('show');
                } else {
                    btn.removeClass('show');
                }
            });
            btn.on('click', function(e) {
                e.preventDefault();
                $('html, body').animate({
                    scrollTop: 0
                }, '300');
            });
        },

        shopFilterWidget: function() {
            $('.toggle-list > .title').on('click', function(e) {

                var target = $(this).parent().children('.shop-submenu');
                var target2 = $(this).parent();
                $(target).slideToggle();
                $(target2).toggleClass('active');
            });

            $('.toggle-btn').on('click', function(e) {

                var target = $(this).parent().siblings('.toggle-open');
                var target2 = $(this).parent();
                $(target).slideToggle();
                $(target2).toggleClass('active');
            });
        },

        mobileMenuActivation: function(e) {
            
            $('.menu-item-has-children > a').on('click', function(e) {

                var targetParent = $(this).parents('.header-main-nav');
                var target = $(this).siblings('.axil-submenu');

                if (targetParent.hasClass('open')) {
                    $(target).slideToggle(400);
                    $(this).parent('.menu-item-has-children').toggleClass('open');
                }

            });

            $('.nav-link.has-megamenu').on('click', function(e) {

                var $this = $(this),
                targetElm = $this.siblings('.megamenu-mobile-toggle');
                targetElm.slideToggle(500);
            });

            // Mobile Sidemenu Class Add
            function resizeClassAdd() {
                if (window.matchMedia('(max-width: 1199px)').matches) {
                    $('.department-title').addClass('department-side-menu');
                    $('.department-megamenu').addClass('megamenu-mobile-toggle');
                } else {
                    $('.department-title').removeClass('department-side-menuu');
                    $('.department-megamenu').removeClass('megamenu-mobile-toggle').removeAttr('style');
                }
            }

            $(window).resize(function() {
                resizeClassAdd();
            });

            resizeClassAdd();
        },

        menuLinkActive: function () {
            var currentPage = location.pathname.split("/"),
                current = currentPage[currentPage.length-1];
            $('.mainmenu li a, .main-navigation li a').each(function(){
                var $this = $(this);
                if($this.attr('href') === current){
                    $this.addClass('active');
                    $this.parents('.menu-item-has-children').addClass('menu-item-open')
                }
            });
        },

        headerIconToggle: function() {

            $('.my-account > a').on('click', function(e) {
                $(this).toggleClass('open').siblings().toggleClass('open');
            })
        },

        priceRangeSlider: function(e) {
            // Check if slider element exists before initializing
            if ($('#slider-range').length === 0) {
                return; // Exit if element doesn't exist
            }
            
            function formatPrice(price) {
                return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
            }
            $('#slider-range').slider({
                range: true,
                min: 0,
                max: 150000000,
                step: 1000000,
                values: [0, 150000000],
                slide: function(event, ui) {
                    $('#amount').val(formatPrice(ui.values[0]) + ' VNĐ - ' + formatPrice(ui.values[1]) + ' VNĐ');
                    $('#price-min').val(ui.values[0]);
                    $('#price-max').val(ui.values[1]);
                },
                stop: function(event, ui) {
                }
            });
            var initialMin = $('#slider-range').slider('values', 0);
            var initialMax = $('#slider-range').slider('values', 1);
            $('#amount').val(formatPrice(initialMin) + ' VNĐ - ' + formatPrice(initialMax) + ' VNĐ');

            $('#price-min').val(initialMin);
            $('#price-max').val(initialMax);
        },

        quantityRanger: function() {
            $('.pro-qty').prepend('<span class="dec qtybtn">-</span>');
            $('.pro-qty').append('<span class="inc qtybtn">+</span>');
            $('.qtybtn').on('click', function() {
                var $button = $(this);
                var oldValue = $button.parent().find('input').val();
                if ($button.hasClass('inc')) {
                    var newVal = parseFloat(oldValue) + 1;
                } else {if (oldValue > 0) {
                        var newVal = parseFloat(oldValue) - 1;
                    } else {
                        newVal = 0;
                    }
                }
                $button.parent().find('input').val(newVal);
            });
        },

        axilSlickActivation: function(e) {
            // Helper function to safely initialize slick slider
            function safeSlickInit(selector, options) {
                try {
                    var $element = $(selector);
                    if ($element.length > 0 && !$element.hasClass('slick-initialized')) {
                        // Add a small delay to ensure DOM is ready
                        setTimeout(function() {
                            try {
                                $element.slick(options);
                            } catch (error) {
                                console.warn('Slick initialization failed for ' + selector + ':', error);
                            }
                        }, 100);
                    }
                } catch (error) {
                    console.warn('Slick selector check failed for ' + selector + ':', error);
                }
            }

            // Category product activation
            safeSlickInit('.categrie-product-activation', {
                infinite: true,
                slidesToShow: 7,
                slidesToScroll: 7,
                arrows: true,
                dots: false,
                autoplay: false,
                speed: 1000,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [
                    {
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 6,
                            slidesToScroll: 6
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 4,
                            slidesToScroll: 4
                        }
                    },
                    {
                        breakpoint: 767,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 479,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 400,
                        settings: {
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });

            // Category product activation 3
            safeSlickInit('.categrie-product-activation-3', {
                infinite: true,
                slidesToShow: 6,
                slidesToScroll: 6,
                arrows: true,
                dots: false,
                autoplay: false,
                speed: 1000,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [
                    {
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 5,
                            slidesToScroll: 5
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 4,
                            slidesToScroll: 4
                        }
                    },
                    {
                        breakpoint: 767,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 479,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 400,
                        settings: {
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });

            // Category product activation 4
            safeSlickInit('.categrie-product-activation-4', {
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                autoplay: false,
                speed: 1000,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-angle-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-angle-right"></i></button>'
            });

            // Category product activation 2
            safeSlickInit('.categrie-product-activation-2', {
                infinite: true,
                slidesToShow: 7,
                slidesToScroll: 7,
                arrows: true,
                dots: false,
                autoplay: true,
                speed: 1000,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [
                    {
                        breakpoint: 1399,
                        settings: {
                            slidesToShow: 6,
                            slidesToScroll: 6
                        }
                    },
                    {
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 5,
                            slidesToScroll: 5
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 767,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 479,
                        settings: {
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });

            // Explore product activation
            safeSlickInit('.explore-product-activation', {
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>'
            });

            // Popular product activation
            safeSlickInit('.popular-product-activation', {
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-angle-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-angle-right"></i></button>'
            });

            // New arrivals product activation
            safeSlickInit('.new-arrivals-product-activation', {
                infinite: true,
                slidesToShow: 4,
                slidesToScroll: 4,
                arrows: true,
                dots: false,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [{
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 576,
                        settings: {
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });

            // New arrivals product activation 2
            safeSlickInit('.new-arrivals-product-activation-2', {
                infinite: true,
                slidesToShow: 4,
                slidesToScroll: 4,
                arrows: true,
                dots: false,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [{
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 576,
                        settings: {
                            variableWidth: true,
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });

            // Recently viewed activation
            safeSlickInit('.recently-viwed-activation', {
                infinite: true,
                slidesToShow: 4,
                slidesToScroll: 4,
                arrows: true,
                dots: false,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-angle-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-angle-right"></i></button>',
                responsive: [{
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 576,
                        settings: {
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });

            // Recent product activation
            safeSlickInit('.recent-product-activation', {
                infinite: true,
                slidesToShow: 4,
                slidesToScroll: 4,
                arrows: true,
                dots: false,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [{
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 479,
                        settings: {
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });

            // Header campaign activation
            safeSlickInit('.header-campaign-activation', {
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                autoplay: true,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>'
            });

            // Testimonial slick activation two
            safeSlickInit('.testimonial-slick-activation-two', {
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: true,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>'
            });

            // Testimonial slick activation
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
                responsive: [{
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 1
                        }
                    }
                ]
            });

            // Testimonial slick activation three with custom slide counter
            var $slideStatus = $('.slick-slide-count');
            if ($('.testimonial-slick-activation-three').length > 0) {
                $('.testimonial-slick-activation-three').on('init reInit afterChange', function (event, slick, currentSlide, nextSlide) {
                    var i = (currentSlide ? currentSlide : 0) + 1;
                    $slideStatus.text(i + '/' + slick.slideCount);
                });

                $('.testimonial-slick-activation-three').slick({
                    infinite: true,
                    slidesToShow: 1,
                    slidesToScroll: 1,
                    arrows: true,
                    dots: false,
                    speed: 500,
                    draggable: true,
                    prevArrow: $('.prev-custom-nav'),
                    nextArrow: $('.next-custom-nav'),
                    responsive: [{
                            breakpoint: 991,
                            settings: {
                                slidesToShow: 1
                            }
                        }
                    ]
                });
            }

            // Product small thumb
            safeSlickInit('.product-small-thumb', {
                infinite: false,
                slidesToShow: 6,
                slidesToScroll: 1,
                arrows: false,
                dots: false,
                focusOnSelect: true,
                vertical: true,
                speed: 800,
                asNavFor: '.product-large-thumbnail',
                responsive: [{
                        breakpoint: 992,
                        settings: {
                            vertical: false
                        }
                    },
                    {
                        breakpoint: 768,
                        settings: {
                            vertical: false,
                            slidesToShow: 4
                        }
                    }
                ]
            });

            // Product large thumbnail
            safeSlickInit('.product-large-thumbnail', {
                infinite: false,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: false,
                dots: false,
                speed: 800,
                draggable: false,
                asNavFor: '.product-small-thumb'
            });

            // Product small thumb 2
            safeSlickInit('.product-small-thumb-2', {
                infinite: true,
                slidesToShow: 6,
                slidesToScroll: 1,
                arrows: false,
                dots: false,
                focusOnSelect: true,
                speed: 800,
                asNavFor: '.product-large-thumbnail-2',
                responsive: [{
                        breakpoint: 768,
                        settings: {
                            slidesToShow: 5
                        }
                    },
                    {
                        breakpoint: 479,
                        settings: {
                            slidesToShow: 4
                        }
                    }
                ]
            });

            // Product large thumbnail 2
            safeSlickInit('.product-large-thumbnail-2', {
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                speed: 800,
                draggable: false,
                asNavFor: '.product-small-thumb-2',
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>'
            });

            // Product small thumb 3
            safeSlickInit('.product-small-thumb-3', {
                infinite: false,
                slidesToShow: 4,
                slidesToScroll: 1,
                arrows: false,
                dots: false,
                focusOnSelect: true,
                vertical: true,
                speed: 800,
                draggable: false,
                swipe: false,
                asNavFor: '.product-large-thumbnail-3',
                responsive: [{
                        breakpoint: 992,
                        settings: {
                            vertical: false
                        }
                    }
                ]
            });

            // Product large thumbnail 3
            safeSlickInit('.product-large-thumbnail-3', {
                infinite: false,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: false,
                dots: false,
                speed: 800,
                draggable: false,
                swipe: false,
                asNavFor: '.product-small-thumb-3'
            });

            // Product small thumb 4
            safeSlickInit('.product-small-thumb-4', {
                infinite: true,
                slidesToShow: 5,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                focusOnSelect: true,
                speed: 800,
                asNavFor: '.product-large-thumbnail-4',
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-angle-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-angle-right"></i></button>',
                responsive: [{
                        breakpoint: 768,
                        settings: {
                            slidesToShow: 5
                        }
                    },
                    {
                        breakpoint: 479,
                        settings: {
                            slidesToShow: 4
                        }
                    }
                ]
            });

            // Product large thumbnail 4
            safeSlickInit('.product-large-thumbnail-4', {
                infinite: false,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: false,
                dots: false,
                speed: 800,
                draggable: false,
                swipe: false,
                asNavFor: '.product-small-thumb-4'
            });

            // Related blog activation
            safeSlickInit('.related-blog-activation', {
                infinite: true,
                slidesToShow: 3,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                speed: 500,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [{
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 2
                        }
                    },
                    {
                        breakpoint: 768,
                        settings: {
                            slidesToShow: 1
                        }
                    }
                ]
            });

            // Blog gallery activation
            safeSlickInit('.blog-gallery-activation', {
                infinite: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: true,
                dots: false,
                speed: 500,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>'
            });

            $('#quick-view-modal').on('shown.bs.modal', function (event) {
                $('.slick-slider').slick('setPosition');
            });

            // Custom slider initialization moved to homepage-specific script
            // $('.slider-thumb-activation-one').slick({
            //     infinite: true,
            //     slidesToShow: 2,
            //     slidesToScroll: 1,
            //     arrows: false,
            //     dots: true,
            //     focusOnSelect: false,
            //     speed: 1000,
            //     autoplay: false,
            //     asNavFor: '.slider-content-activation-one',
            //     prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
            //     nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
            //     responsive: [{
            //             breakpoint: 991,
            //             settings: {
            //                 slidesToShow: 1,
            //             }
            //         }
            //     ]

            // });

            // Slider thumb activation two
            safeSlickInit('.slider-thumb-activation-two', {
                infinite: true,
                slidesToShow: 3,
                centerPadding: '0',
                arrows: false,
                dots: true,
                speed: 1500,
                autoplay: false,
                centerMode: true,
                responsive: [{
                        breakpoint: 575,
                        settings: {
                            slidesToShow: 1
                        }
                    }
                ]
            });

            // Slider thumb activation three
            safeSlickInit('.slider-thumb-activation-three', {
                infinite: true,
                slidesToShow: 2,
                slidesToScroll: 1,
                arrows: false,
                dots: false,
                focusOnSelect: false,
                speed: 1500,
                autoplay: true,
                responsive: [{
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 1
                        }
                    }
                ]
            });

            // Slider activation one
            safeSlickInit('.slider-activation-one', {
                infinite: true,
                autoplay: true,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: false,
                dots: true,
                fade: true,
                focusOnSelect: false,
                speed: 400
            });

            // Slider activation two
            safeSlickInit('.slider-activation-two', {
                infinite: true,
                autoplay: false,
                slidesToShow: 1,
                slidesToScroll: 1,
                arrows: false,
                dots: true,
                fade: true,
                adaptiveHeight: true,
                cssEase: 'linear',
                speed: 400
            });

            // Team slide activation
            safeSlickInit('.team-slide-activation', {
                infinite: true,
                slidesToShow: 4,
                slidesToScroll: 4,
                arrows: true,
                dots: false,
                prevArrow: '<button class="slide-arrow prev-arrow"><i class="fal fa-long-arrow-left"></i></button>',
                nextArrow: '<button class="slide-arrow next-arrow"><i class="fal fa-long-arrow-right"></i></button>',
                responsive: [{
                        breakpoint: 1199,
                        settings: {
                            slidesToShow: 3,
                            slidesToScroll: 3
                        }
                    },
                    {
                        breakpoint: 991,
                        settings: {
                            slidesToShow: 2,
                            slidesToScroll: 2
                        }
                    },
                    {
                        breakpoint: 576,
                        settings: {
                            slidesToShow: 1,
                            slidesToScroll: 1
                        }
                    }
                ]
            });
        },

        countdownInit: function(countdownSelector, countdownTime) {
            var eventCounter = $(countdownSelector);
            if (eventCounter.length) {
                eventCounter.countdown(countdownTime, function(e) {
                    $(this).html(
                        e.strftime(
                            "<div class='countdown-section'><div><div class='countdown-number'>%-D</div> <div class='countdown-unit'>Day</div> </div></div><div class='countdown-section'><div><div class='countdown-number'>%H</div> <div class='countdown-unit'>Hrs</div> </div></div><div class='countdown-section'><div><div class='countdown-number'>%M</div> <div class='countdown-unit'>Min</div> </div></div><div class='countdown-section'><div><div class='countdown-number'>%S</div> <div class='countdown-unit'>Sec</div> </div></div>"
                        )
                    );
                });
            }
        },

        campaignCountdown: function(countdownSelector, countdownTime) {
            var eventCounter = $(countdownSelector);
            if (eventCounter.length) {
                eventCounter.countdown(countdownTime, function(e) {
                    $(this).html(
                        e.strftime(
                            "<div class='countdown-section'><div><div class='countdown-number'>%-D</div> <div class='countdown-unit'>D</div> </div></div><div class='countdown-section'><div><div class='countdown-number'>%H</div> <div class='countdown-unit'>H</div> </div></div><div class='countdown-section'><div><div class='countdown-number'>%M</div> <div class='countdown-unit'>M</div> </div></div><div class='countdown-section'><div><div class='countdown-number'>%S</div> <div class='countdown-unit'>S</div> </div></div>"
                        )
                    );
                });
            }
        },

        sideOffcanvasToggle: function(selectbtn, openElement) {

            $('body').on('click', selectbtn, function(e) {
                e.preventDefault();

                var $this = $(this),
                    wrapp = $this.parents('body'),
                    wrapMask = $('<div / >').addClass('closeMask'),
                    cartDropdown = $(openElement);

                if (!(cartDropdown).hasClass('open')) {
                    wrapp.addClass('open');
                    cartDropdown.addClass('open');
                    cartDropdown.parent().append(wrapMask);
                    wrapp.css({
                        'overflow': 'hidden'

                    });

                } else {
                    removeSideMenu();
                }

                function removeSideMenu() {
                    wrapp.removeAttr('style');
                    wrapp.removeClass('open').find('.closeMask').remove();
                    cartDropdown.removeClass('open');
                }

                $('.sidebar-close, .closeMask').on('click', function() {
                    removeSideMenu();
                });

            });
        },

        stickyHeaderMenu: function() {

            $(window).on('scroll', function() {
                // Sticky Class Add
                if ($('body').hasClass('sticky-header')) {
                    var stickyPlaceHolder = $('#axil-sticky-placeholder'),
                        menu = $('.axil-mainmenu'),
                        menuH = menu.outerHeight(),
                        topHeaderH = $('.axil-header-top').outerHeight() || 0,
                        headerCampaign = $('.header-top-campaign').outerHeight() || 0,
                        targrtScroll = topHeaderH + headerCampaign;
                    if ($(window).scrollTop() > targrtScroll) {
                        menu.addClass('axil-sticky');
                        stickyPlaceHolder.height(menuH);
                    } else {
                        menu.removeClass('axil-sticky');
                        stickyPlaceHolder.height(0);
                    }
                }
            });
        },

        salActivation: function() {
            sal({
                threshold: 0.3,
                once: true
            });
        },

        magnificPopupActivation: function() {

            var yPopup = $('.popup-youtube');
            if (yPopup.length) {
                yPopup.magnificPopup({
                    disableOn: 300,
                    type: 'iframe',
                    mainClass: 'mfp-fade',
                    removalDelay: 160,
                    preloader: false,
                    fixedContentPos: false
                });
            }

            if ($('.zoom-gallery').length) {
                $('.zoom-gallery').each(function() {
                    $(this).magnificPopup({
                        delegate: 'a.popup-zoom',
                        type: 'image',
                        gallery: {
                            enabled: true
                        }
                    });
                });
            }
        },

        colorVariantActive: function() {
            $('.color-variant > li').on('click', function(e) {
                $(this).addClass('active').siblings().removeClass('active');
            })
        },

        headerCampaignRemove: function() {
           $('.remove-campaign').on('click', function() {
                var targetElem = $('.header-top-campaign');
                targetElem.slideUp(function() {
                    $(this).remove();
                });
           });
        },

        offerPopupActivation: function() {
            if ($('body').hasClass('newsletter-popup-modal')) {
                setTimeout(function(){ 
                    $('body').addClass('open');
                    $('#offer-popup-modal').addClass('open');
                }, 1000);
            }
        },

        axilMasonary: function () {
            $('.axil-isotope-wrapper').imagesLoaded(function () {
                // filter items on button click
                $('.isotope-button').on('click', 'button', function () {
                    var filterValue = $(this).attr('data-filter');
                    $grid.isotope({
                        filter: filterValue
                    });
                });
                
                // init Isotope
                var $grid = $('.isotope-list').isotope({
                    itemSelector: '.product',
                    percentPosition: true,
                    transitionDuration: '0.7s',
                    layoutMode: 'fitRows',
                    masonry: {
                        // use outer width of grid-sizer for columnWidth
                        columnWidth: 1,
                    }
                });
            });
        
            $('.isotope-button button').on('click', function (event) {
                $(this).siblings('.is-checked').removeClass('is-checked');
                $(this).addClass('is-checked');
                event.preventDefault();
            });
        },

        onLoadClassAdd: function () {
            this._window.on( "load", function() {
                setTimeout(function() {
                    $('.main-slider-style-4').addClass('animation-init');
                }, 500);
            });

        },

        dropdownMenuSlide: function () {
            if (window.matchMedia('(max-width: 991px)').matches) {
                $('#dropdown-header-menu').removeAttr('data-bs-toggle');
                $('#dropdown-header-menu').on('click', function() {
                   $(this).siblings('.dropdown-menu').slideToggle();
            
                })
            }

        },

    }
    axilInit.i();

})(window, document, jQuery);