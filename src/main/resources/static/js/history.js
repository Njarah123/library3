document.addEventListener('DOMContentLoaded', function() {
    // Animation des lignes du tableau
    const tableRows = document.querySelectorAll('.history-table tbody tr');
    
    tableRows.forEach((row, index) => {
        row.style.opacity = '0';
        row.style.transform = 'translateX(-20px)';
        
        setTimeout(() => {
            row.style.transition = 'all 0.5s ease';
            row.style.opacity = '1';
            row.style.transform = 'translateX(0)';
        }, index * 100);
    });

    // Système de notation amélioré
    const ratingForms = document.querySelectorAll('.star-rating');
    
    ratingForms.forEach(form => {
        const stars = form.querySelectorAll('label');
        const submitBtn = form.querySelector('.btn-submit-rating');
        
        stars.forEach(star => {
            star.addEventListener('mouseover', function() {
                const rating = this.getAttribute('for').split('-')[0];
                highlightStars(stars, rating);
            });

            star.addEventListener('mouseout', function() {
                const checkedInput = form.querySelector('input:checked');
                const rating = checkedInput ? checkedInput.value : 0;
                highlightStars(stars, rating);
            });
        });

        if (submitBtn) {
            submitBtn.addEventListener('click', function(e) {
                const rating = form.querySelector('input:checked');
                const comment = form.querySelector('textarea');
                
                if (!rating) {
                    e.preventDefault();
                    alert('Veuillez sélectionner une note');
                    return;
                }

                // Animation du bouton
                this.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    this.style.transform = '';
                }, 100);
            });
        }
    });

    // Fonction pour mettre en surbrillance les étoiles
    function highlightStars(stars, rating) {
        stars.forEach(star => {
            const starRating = star.getAttribute('for').split('-')[0];
            star.style.color = starRating <= rating ? '#ffd700' : '#ddd';
        });
    }

    // Animation du titre
    const title = document.querySelector('h1');
    if (title) {
        title.style.opacity = '0';
        title.style.transform = 'translateY(-20px)';
        
        requestAnimationFrame(() => {
            title.style.transition = 'all 0.6s ease';
            title.style.opacity = '1';
            title.style.transform = 'translateY(0)';
        });
    }

    // Animation du bouton retour
    const backButton = document.querySelector('.back-button');
    if (backButton) {
        backButton.addEventListener('mousemove', (e) => {
            const rect = backButton.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            backButton.style.setProperty('--x', `${x}px`);
            backButton.style.setProperty('--y', `${y}px`);
        });
    }
});