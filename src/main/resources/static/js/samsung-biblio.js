// Script JavaScript pour la Bibliothèque Joanjarah

document.addEventListener('DOMContentLoaded', function() {
    
    // ========================================
    // Navigation Mobile
    // ========================================
    const navToggle = document.querySelector('.nav-toggle');
    const navMenu = document.querySelector('.nav-menu');
    
    navToggle.addEventListener('click', function() {
        navToggle.classList.toggle('active');
        navMenu.classList.toggle('active');
    });
    
    // Fermer le menu mobile quand on clique sur un lien
    document.querySelectorAll('.nav-menu a').forEach(link => {
        link.addEventListener('click', () => {
            navToggle.classList.remove('active');
            navMenu.classList.remove('active');
        });
    });

    // ========================================
    // Carrousel d'images Hero (Scroll Infini)
    // ========================================
    const heroImages = document.querySelectorAll('.hero-image');
    let currentImageIndex = 0;
    
    function showNextImage() {
        // Retirer la classe active de l'image actuelle
        heroImages[currentImageIndex].classList.remove('active');
        
        // Passer à l'image suivante
        currentImageIndex = (currentImageIndex + 1) % heroImages.length;
        
        // Ajouter la classe active à la nouvelle image
        heroImages[currentImageIndex].classList.add('active');
    }
    
    // Démarrer le carrousel automatique
    setInterval(showNextImage, 4000); // Change d'image toutes les 4 secondes

    // ========================================
    // Scroll Animation Navbar
    // ========================================
    const navbar = document.querySelector('.navbar');
    let lastScrollY = window.scrollY;
    
    window.addEventListener('scroll', function() {
        const currentScrollY = window.scrollY;
        
        if (currentScrollY > 100) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
            navbar.style.boxShadow = '0 2px 20px rgba(0,0,0,0.1)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.boxShadow = 'none';
        }
        
        // Cacher/montrer la navbar selon la direction du scroll
        if (currentScrollY > lastScrollY && currentScrollY > 200) {
            navbar.style.transform = 'translateY(-100%)';
        } else {
            navbar.style.transform = 'translateY(0)';
        }
        
        lastScrollY = currentScrollY;
    });

    // ========================================
    // Animations de Scroll (Intersection Observer)
    // ========================================
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
            }
        });
    }, observerOptions);
    
    // Ajouter les classes d'animation aux éléments
    function addScrollAnimations() {
        // Cartes de services
        document.querySelectorAll('.service-card').forEach((card, index) => {
            card.classList.add('fade-in');
            card.style.animationDelay = `${index * 0.2}s`;
            observer.observe(card);
        });
        
        // Cartes de collections
        document.querySelectorAll('.collection-card').forEach((card, index) => {
            card.classList.add('fade-in');
            card.style.animationDelay = `${index * 0.15}s`;
            observer.observe(card);
        });
        
        // Textes de description
        document.querySelectorAll('.description-text').forEach((text, index) => {
            text.classList.add('fade-in');
            text.style.animationDelay = `${index * 0.3}s`;
            observer.observe(text);
        });
        
        // Titres de section
        document.querySelectorAll('.section-title').forEach(title => {
            title.classList.add('fade-in');
            observer.observe(title);
        });
        
        // Éléments de contact
        document.querySelectorAll('.contact-item').forEach((item, index) => {
            item.classList.add('fade-in');
            item.style.animationDelay = `${index * 0.2}s`;
            observer.observe(item);
        });
    }
    
    addScrollAnimations();

    // ========================================
    // Smooth Scroll pour les liens d'ancrage
    // ========================================
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

    // ========================================
    // Parallax Effect pour les images
    // ========================================
    function handleParallax() {
        const scrolled = window.pageYOffset;
        const parallaxElements = document.querySelectorAll('.large-image-container img, .architecture-image img');
        
        parallaxElements.forEach(element => {
            const rect = element.getBoundingClientRect();
            const speed = 0.5;
            
            if (rect.bottom >= 0 && rect.top <= window.innerHeight) {
                const yPos = -(scrolled * speed);
                element.style.transform = `scale(1.1) translateY(${yPos}px)`;
            }
        });
    }
    
    window.addEventListener('scroll', handleParallax);

    // ========================================
    // Hover Effects pour les cartes
    // ========================================
    function addHoverEffects() {
        const cards = document.querySelectorAll('.service-card, .collection-card');
        
        cards.forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-10px) scale(1.02)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
            });
        });
    }
    
    addHoverEffects();

    // ========================================
    // Animation de typing pour le titre hero
    // ========================================
    function typeWriter(element, text, speed = 100) {
        let i = 0;
        element.innerHTML = '';
        
        function type() {
            if (i < text.length) {
                element.innerHTML += text.charAt(i);
                i++;
                setTimeout(type, speed);
            }
        }
        
        type();
    }

    // ========================================
    // Gestion du redimensionnement de fenêtre
    // ========================================
    let resizeTimer;
    window.addEventListener('resize', function() {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(function() {
            // Recalculer les animations si nécessaire
            handleParallax();
        }, 250);
    });

    // ========================================
    // Préchargement des images
    // ========================================
    function preloadImages() {
        const images = [
            'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=1920&h=1080&fit=crop&crop=faces',
            'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=1920&h=1080&fit=crop',
            'https://images.unsplash.com/photo-1519452634681-4d2e18b8da35?w=1920&h=1080&fit=crop',
            'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=1920&h=1080&fit=crop',
            'https://images.unsplash.com/photo-1526243741027-444d633d7365?w=1920&h=1080&fit=crop'
        ];
        
        images.forEach(src => {
            const img = new Image();
            img.src = src;
        });
    }
    
    preloadImages();

    // ========================================
    // Gestion du scroll fluide avancé
    // ========================================
    let ticking = false;
    
    function updateScrollAnimations() {
        // Mettre à jour les animations basées sur le scroll
        const scrollTop = window.pageYOffset;
        const windowHeight = window.innerHeight;
        
        // Animation de la navbar
        if (scrollTop > 50) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
        
        // Animation du scroll indicator
        const scrollIndicator = document.querySelector('.scroll-indicator');
        if (scrollIndicator) {
            const opacity = Math.max(0, 1 - (scrollTop / windowHeight));
            scrollIndicator.style.opacity = opacity;
        }
        
        ticking = false;
    }
    
    function requestTick() {
        if (!ticking) {
            requestAnimationFrame(updateScrollAnimations);
            ticking = true;
        }
    }
    
    window.addEventListener('scroll', requestTick);

    // ========================================
    // Détection de la vitesse de scroll
    // ========================================
    let scrollSpeed = 0;
    let lastScrollTime = Date.now();
    
    window.addEventListener('scroll', function() {
        const now = Date.now();
        const timeDiff = now - lastScrollTime;
        const scrollDiff = Math.abs(window.pageYOffset - lastScrollY);
        
        scrollSpeed = scrollDiff / timeDiff;
        lastScrollTime = now;
        
        // Ajuster les animations en fonction de la vitesse de scroll
        if (scrollSpeed > 1) {
            document.body.classList.add('fast-scroll');
        } else {
            document.body.classList.remove('fast-scroll');
        }
    });

    // ========================================
    // Gestion de l'accessibilité
    // ========================================
    
    // Réduire les animations si l'utilisateur préfère
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        document.body.classList.add('reduced-motion');
    }
    
    // Gestion du focus pour l'accessibilité
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Tab') {
            document.body.classList.add('keyboard-navigation');
        }
    });
    
    document.addEventListener('mousedown', function() {
        document.body.classList.remove('keyboard-navigation');
    });

    // ========================================
    // Lazy Loading des images
    // ========================================
    const lazyImages = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                imageObserver.unobserve(img);
            }
        });
    });
    
    lazyImages.forEach(img => {
        imageObserver.observe(img);
    });

    // ========================================
    // Animation de chargement
    // ========================================
    window.addEventListener('load', function() {
        document.body.classList.add('loaded');
        
        // Animer les éléments de la page d'accueil
        setTimeout(() => {
            document.querySelector('.hero-title').classList.add('animate');
            document.querySelector('.hero-description').classList.add('animate');
        }, 300);
    });

    // ========================================
    // Gestion des erreurs d'images
    // ========================================
    document.querySelectorAll('img').forEach(img => {
        img.addEventListener('error', function() {
            this.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZGRkIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCwgc2Fucy1zZXJpZiIgZm9udC1zaXplPSIxNCIgZmlsbD0iIzk5OSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPkltYWdlIG5vbiBkaXNwb25pYmxlPC90ZXh0Pjwvc3ZnPg==';
        });
    });

    // ========================================
    // Console log pour le debug
    // ========================================
    console.log('🏛️ Bibliothèque Joanjarah - Site chargé avec succès!');
    console.log('📚 Expanding Knowledge - Tous les scripts sont actifs');
    
});

// ========================================
// Fonctions utilitaires
// ========================================

// Fonction pour obtenir la position d'un élément
function getElementPosition(element) {
    const rect = element.getBoundingClientRect();
    return {
        top: rect.top + window.pageYOffset,
        left: rect.left + window.pageXOffset,
        width: rect.width,
        height: rect.height
    };
}

// Fonction pour vérifier si un élément est visible
function isElementInViewport(element) {
    const rect = element.getBoundingClientRect();
    return (
        rect.top >= 0 &&
        rect.left >= 0 &&
        rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
        rect.right <= (window.innerWidth || document.documentElement.clientWidth)
    );
}

// Fonction de debounce pour optimiser les performances
function debounce(func, wait, immediate) {
    let timeout;
    return function executedFunction() {
        const context = this;
        const args = arguments;
        const later = function() {
            timeout = null;
            if (!immediate) func.apply(context, args);
        };
        const callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func.apply(context, args);
    };
}

// Fonction de throttle pour limiter les appels
function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}