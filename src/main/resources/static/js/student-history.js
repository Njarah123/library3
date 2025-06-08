document.addEventListener('DOMContentLoaded', function() {
    // Animation des cartes statistiques
    const statCards = document.querySelectorAll('.stat-card');
    
    statCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);

        // Animation du compteur
        const number = card.querySelector('p');
        const finalValue = parseInt(number.textContent);
        let currentValue = 0;
        
        const increment = finalValue / 50;
        const duration = 1500;
        const interval = duration / 50;

        const counter = setInterval(() => {
            currentValue += increment;
            if (currentValue >= finalValue) {
                currentValue = finalValue;
                clearInterval(counter);
            }
            number.textContent = Math.round(currentValue);
        }, interval);
    });

    // Animation des lignes du tableau
    const tableRows = document.querySelectorAll('tbody tr');
    
    tableRows.forEach((row, index) => {
        row.style.opacity = '0';
        row.style.transform = 'translateX(-20px)';
        
        setTimeout(() => {
            row.style.transition = 'all 0.5s ease';
            row.style.opacity = '1';
            row.style.transform = 'translateX(0)';
        }, (index * 100) + 500);
    });

    // Amélioration du système de notation
    const ratingForms = document.querySelectorAll('.rating-form');
    
    ratingForms.forEach(form => {
        const select = form.querySelector('select');
        const button = form.querySelector('button');
        
        select.addEventListener('change', () => {
            button.style.transform = 'scale(1.1)';
            setTimeout(() => {
                button.style.transform = 'scale(1)';
            }, 200);
        });

        form.addEventListener('submit', (e) => {
            e.preventDefault();
            if (select.value) {
                button.style.transform = 'scale(0.95)';
                setTimeout(() => {
                    form.submit();
                }, 200);
            }
        });
    });

    // Animation des notes existantes
    const ratings = document.querySelectorAll('.rating');
    ratings.forEach(rating => {
        rating.style.opacity = '0';
        rating.style.transform = 'scale(0.8)';
        
        setTimeout(() => {
            rating.style.transition = 'all 0.5s ease';
            rating.style.opacity = '1';
            rating.style.transform = 'scale(1)';
        }, 1000);
    });

    // Animation du bouton retour
    const backButton = document.querySelector('.btn-back');
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