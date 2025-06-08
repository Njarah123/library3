document.addEventListener('DOMContentLoaded', function() {
    // Animation d'apparition des livres
    const bookCards = document.querySelectorAll('.book-card');
    
    bookCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // Effet de parallaxe sur les couvertures
    bookCards.forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const xPercent = (x / rect.width - 0.5) * 20;
            const yPercent = (y / rect.height - 0.5) * 20;
            
            const cover = card.querySelector('.book-cover');
            cover.style.transform = `perspective(1000px) rotateY(${xPercent}deg) rotateX(${-yPercent}deg)`;
        });

        card.addEventListener('mouseleave', (e) => {
            const cover = card.querySelector('.book-cover');
            cover.style.transform = 'perspective(1000px) rotateY(0) rotateX(0)';
        });
    });

    // Confirmation de suppression stylée
    document.querySelectorAll('.btn-delete').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            
            if (confirm('Êtes-vous sûr de vouloir supprimer ce livre ?')) {
                this.closest('form').submit();
            }
        });
    });

    // Animation de la barre de recherche
    const searchInput = document.querySelector('.search-input');
    
    searchInput.addEventListener('focus', () => {
        searchInput.parentElement.style.transform = 'scale(1.02)';
    });

    searchInput.addEventListener('blur', () => {
        searchInput.parentElement.style.transform = 'scale(1)';
    });
});
// Animation du bouton d'accueil
document.addEventListener('DOMContentLoaded', function() {
    const homeButton = document.querySelector('.btn-home');
    
    if (homeButton) {
        // Animation d'entrée
        homeButton.style.opacity = '0';
        homeButton.style.transform = 'translateX(-20px)';
        
        setTimeout(() => {
            homeButton.style.transition = 'all 0.5s ease';
            homeButton.style.opacity = '1';
            homeButton.style.transform = 'translateX(0)';
        }, 300);

        // Animation au survol
        homeButton.addEventListener('mousemove', (e) => {
            const rect = homeButton.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 20;
            const rotateY = (centerX - x) / 20;

            homeButton.style.transform = `
                perspective(1000px)
                rotateX(${rotateX}deg)
                rotateY(${rotateY}deg)
                translateY(-2px)
            `;
        });

        homeButton.addEventListener('mouseleave', () => {
            homeButton.style.transform = '';
        });

        // Animation au clic
        homeButton.addEventListener('click', function() {
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 100);
        });
    }
});