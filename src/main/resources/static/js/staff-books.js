document.addEventListener('DOMContentLoaded', function() {
    // Animation des cartes de livres
    const bookCards = document.querySelectorAll('.book-card');
    const HOVER_SCALE = 1.05; // Définir la valeur de scale directement en JS
    
    bookCards.forEach((card, index) => {
        // Animation initiale
        card.style.opacity = '0';
        card.style.animation = `fadeIn 0.6s ease-out ${index * 0.1}s forwards`;

        // Effet de parallaxe au survol
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            
            const rotateX = (y - centerY) / 20;
            const rotateY = (centerX - x) / 20;

            card.style.transform = `
                scale(${HOVER_SCALE})
                translateY(-10px)
                perspective(1000px)
                rotateX(${rotateX}deg)
                rotateY(${rotateY}deg)
            `;
        });

        // Réinitialisation au départ de la souris
        card.addEventListener('mouseleave', () => {
            card.style.transform = 'scale(1) translateY(0) rotateX(0) rotateY(0)';
        });

        // Ajouter un effet de transition fluide
        card.style.transition = 'transform 0.3s ease-out';
    });

    // Animation des boutons d'emprunt
    const borrowButtons = document.querySelectorAll('.btn-borrow');
    
    borrowButtons.forEach(button => {
        button.addEventListener('mouseenter', () => {
            button.style.transform = 'translateY(-2px)';
        });

        button.addEventListener('mouseleave', () => {
            button.style.transform = 'translateY(0)';
        });

        button.addEventListener('click', function(e) {
            // Animation de clic
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                this.style.transform = '';
            }, 100);
        });
    });

    // Animation du bouton retour
    const backButton = document.querySelector('.back-button');
    if (backButton) {
        backButton.addEventListener('mouseenter', () => {
            backButton.style.transform = 'translateY(-2px)';
            backButton.style.boxShadow = '0 5px 15px rgba(255, 255, 255, 0.2)';
        });

        backButton.addEventListener('mouseleave', () => {
            backButton.style.transform = 'translateY(0)';
            backButton.style.boxShadow = 'none';
        });
    }

    // Ajout d'effet de hover pour les statuts
    const statusElements = document.querySelectorAll('.status-available, .status-unavailable');
    statusElements.forEach(status => {
        status.style.transition = 'transform 0.3s ease, opacity 0.3s ease';
        
        status.addEventListener('mouseenter', () => {
            status.style.transform = 'scale(1.05)';
            status.style.opacity = '0.8';
        });

        status.addEventListener('mouseleave', () => {
            status.style.transform = 'scale(1)';
            status.style.opacity = '1';
        });
    });

    // Animation du titre
    const title = document.querySelector('h1');
    if (title) {
        title.style.opacity = '0';
        title.style.transform = 'translateY(-20px)';
        
        requestAnimationFrame(() => {
            title.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            title.style.opacity = '1';
            title.style.transform = 'translateY(0)';
        });
    }
});