document.addEventListener('DOMContentLoaded', function() {
    // Animation des cartes statistiques
    const statBoxes = document.querySelectorAll('.stat-box');
    
    statBoxes.forEach((box, index) => {
        box.style.opacity = '0';
        box.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            box.style.transition = 'all 0.5s ease';
            box.style.opacity = '1';
            box.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // Animation des cartes d'action
    const actionCards = document.querySelectorAll('.action-card');
    
    actionCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, (index * 100) + 300);

        // Effet de parallaxe
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const rotateX = (y - rect.height / 2) / 20;
            const rotateY = (x - rect.width / 2) / 20;
            
            card.style.transform = `
                perspective(1000px)
                rotateX(${-rotateX}deg)
                rotateY(${rotateY}deg)
                translateY(-10px)
            `;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = '';
        });
    });

    // Carousel automatique pour les recommandations
    const carousel = document.querySelector('.books-carousel');
    if (carousel) {
        let scrollPosition = 0;
        const cardWidth = 320; // largeur de la carte + gap

        setInterval(() => {
            scrollPosition += cardWidth;
            if (scrollPosition >= carousel.scrollWidth) {
                scrollPosition = 0;
            }
            carousel.scrollTo({
                left: scrollPosition,
                behavior: 'smooth'
            });
        }, 5000);
    }

    // Animation des étoiles
    const ratings = document.querySelectorAll('.rating');
    ratings.forEach(rating => {
        const stars = rating.querySelector('.stars');
        const value = parseFloat(stars.dataset.rating);
        
        stars.style.width = `${value * 20}%`;
        stars.style.animation = 'glow 1s ease-in-out infinite alternate';
    });
});