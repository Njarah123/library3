// Variables globales
let isScrolling = false;
let particles = [];

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    initializeImmersiveExperience();
    createParticles();
    setupScrollAnimations();
    setupParallaxEffects();
    generateQRCode();
});

// Initialiser l
// Variables globales
let isScrolling = false;
let particles = [];

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    initializeImmersiveExperience();
    createParticles();
    setupScrollAnimations();
    setupParallaxEffects();
    generateQRCode();
});

// Initialiser l'expérience immersive
function initializeImmersiveExperience() {
    // Créer le conteneur de particules
    const particlesContainer = document.createElement('div');
    particlesContainer.className = 'particles';
    document.body.appendChild(particlesContainer);
    
    // Animation du titre principal
    animateHeroTitle();
    
    // Démarrer les animations de fond
    startBackgroundAnimations();
    
    console.log('Expérience immersive initialisée');
}

// Animation du titre héro
function animateHeroTitle() {
    const heroTitle = document.querySelector('.hero-title');
    if (heroTitle) {
        heroTitle.style.opacity = '0';
        heroTitle.style.transform = 'translateY(50px) scale(0.8)';
        
        setTimeout(() => {
            heroTitle.style.transition = 'all 1.5s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
            heroTitle.style.opacity = '1';
            heroTitle.style.transform = 'translateY(0) scale(1)';
        }, 500);
    }
}

// Créer les particules flottantes
function createParticles() {
    const particlesContainer = document.querySelector('.particles');
    const particleCount = 50;
    
    for (let i = 0; i < particleCount; i++) {
        createParticle(particlesContainer);
    }
    
    // Créer de nouvelles particules périodiquement
    setInterval(() => {
        if (particles.length < particleCount) {
            createParticle(particlesContainer);
        }
    }, 3000);
}

function createParticle(container) {
    const particle = document.createElement('div');
    particle.className = 'particle';
    
    // Position et taille aléatoires
    const size = Math.random() * 4 + 2;
    const startX = Math.random() * window.innerWidth;
    const duration = Math.random() * 10 + 15;
    const delay = Math.random() * 5;
    
    particle.style.width = size + 'px';
    particle.style.height = size + 'px';
    particle.style.left = startX + 'px';
    particle.style.animationDuration = duration + 's';
    particle.style.animationDelay = delay + 's';
    
    // Opacité aléatoire
    particle.style.opacity = Math.random() * 0.7 + 0.3;
    
    container.appendChild(particle);
    particles.push(particle);
    
    // Supprimer la particule après l'animation
    setTimeout(() => {
        if (particle.parentNode) {
            particle.parentNode.removeChild(particle);
            particles = particles.filter(p => p !== particle);
        }
    }, (duration + delay) * 1000);
}

// Animations de scroll
function setupScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                
                // Animations spécifiques par section
                if (entry.target.classList.contains('stats-section')) {
                    animateStats();
                }
                
                if (entry.target.classList.contains('dashboard-section')) {
                    animateDashboardCards();
                }
            }
        });
    }, observerOptions);
    
    // Observer toutes les sections immersives
    document.querySelectorAll('.immersive-section').forEach(section => {
        observer.observe(section);
    });
}

// Animer les statistiques
function animateStats() {
    const statNumbers = document.querySelectorAll('.stat-number');
    
    statNumbers.forEach((stat, index) => {
        const finalValue = parseInt(stat.textContent);
        let currentValue = 0;
        const increment = finalValue / 50;
        const duration = 2000;
        const stepTime = duration / 50;
        
        setTimeout(() => {
            const counter = setInterval(() => {
                currentValue += increment;
                if (currentValue >= finalValue) {
                    stat.textContent = finalValue;
                    clearInterval(counter);
                } else {
                    stat.textContent = Math.floor(currentValue);
                }
            }, stepTime);
        }, index * 200);
    });
}

// Animer les cartes du dashboard
function animateDashboardCards() {
    const cards = document.querySelectorAll('.immersive-card');
    
    cards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'translateY(50px) rotateX(10deg)';
            card.style.transition = 'all 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
            
            setTimeout(() => {
                card.style.opacity = '1';
                card.style.transform = 'translateY(0) rotateX(0deg)';
            }, 100);
        }, index * 200);
    });
}

// Effets parallax
function setupParallaxEffects() {
    window.addEventListener('scroll', () => {
        if (!isScrolling) {
            requestAnimationFrame(updateParallax);
            isScrolling = true;
        }
    });
}

function updateParallax() {
    const scrolled = window.pageYOffset;
    const rate = scrolled * -0.5;
    
    // Parallax du background
    const background = document.querySelector('.immersive-background');
    if (background) {
        background.style.transform = `translateY(${rate}px)`;
    }
    
    // Parallax des particules
    const particles = document.querySelectorAll('.particle');
    particles.forEach((particle, index) => {
        const speed = 0.2 + (index % 3) * 0.1;
        particle.style.transform = `translateY(${scrolled * speed}px)`;
    });
    
    isScrolling = false;
}

// Animations de fond
function startBackgroundAnimations() {
    // Animation des gradients
    const background = document.querySelector('.immersive-background');
    let hue = 0;
    
    setInterval(() => {
        hue = (hue + 1) % 360;
        const filter = `hue-rotate(${hue}deg) brightness(1.1)`;
        background.style.filter = filter;
    }, 100);
}

// Générer le QR Code immersif
function generateQRCode() {
    const userData = {
        name: document.querySelector('[data-user-name]')?.dataset.userName || 'Bibliothécaire',
        email: document.querySelector('[data-user-email]')?.dataset.userEmail || 'admin@library.com',
        employeeId: document.querySelector('[data-user-id]')?.dataset.userId || 'LIB001',
        role: 'Bibliothécaire'
    };

    const qrData = `BEGIN:VCARD
VERSION:3.0
FN:${userData.name}
ORG:Bibliothèque Immersive
TITLE:${userData.role}
EMAIL:${userData.email}
NOTE:ID: ${userData.employeeId}
END:VCARD`;

    const qrcodeContainer = document.getElementById('qrcode');
    if (qrcodeContainer && typeof QRCode !== 'undefined') {
        qrcodeContainer.innerHTML = '';
        
        new QRCode(qrcodeContainer, {
            text: qrData,
            width: 200,
            height: 200,
            colorDark: "#000000",
            colorLight: "#ffffff",
            correctLevel: QRCode.CorrectLevel.M
        });
        
        // Animation du QR code
        setTimeout(() => {
            const qrImg = qrcodeContainer.querySelector('img');
            if (qrImg) {
                qrImg.style.opacity = '0';
                qrImg.style.transform = 'scale(0.8) rotate(5deg)';
                qrImg.style.transition = 'all 1s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
                
                setTimeout(() => {
                    qrImg.style.opacity = '1';
                    qrImg.style.transform = 'scale(1) rotate(0deg)';
                }, 100);
            }
        }, 500);
    }
}

// Smooth scroll pour les liens internes
function setupSmoothScroll() {
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
}

// Gestion du redimensionnement
window.addEventListener('resize', () => {
    // Recalculer les positions des particules
    particles.forEach(particle => {
        if (parseFloat(particle.style.left) > window.innerWidth) {
            particle.style.left = Math.random() * window.innerWidth + 'px';
        }
    });
});

// Effets de survol avancés
function setupAdvancedHoverEffects() {
    document.querySelectorAll('.immersive-card').forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-15px) scale(1.03) rotateX(5deg)';
            this.style.boxShadow = '0 25px 80px rgba(255, 215, 0, 0.6)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1) rotateX(0deg)';
            this.style.boxShadow = '0 0 30px rgba(255, 215, 0, 0.3)';
        });
    });
}

// Initialiser tous les effets avancés
document.addEventListener('DOMContentLoaded', function() {
    setupSmoothScroll();
    setupAdvancedHoverEffects();
});

// Performance optimization
function optimizePerformance() {
    // Réduire les particules sur mobile
    if (window.innerWidth < 768) {
        const particleElements = document.querySelectorAll('.particle');
        particleElements.forEach((particle, index) => {
            if (index % 2 === 0) {
                particle.remove();
            }
        });
    }
}

// Appeler l'optimisation
window.addEventListener('load', optimizePerformance);
