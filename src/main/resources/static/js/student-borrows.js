document.addEventListener('DOMContentLoaded', function() {
    // Animation des lignes du tableau
    const tableRows = document.querySelectorAll('tbody tr');
    
    tableRows.forEach((row, index) => {
        row.style.opacity = '0';
        row.style.transform = 'translateX(-20px)';
        
        setTimeout(() => {
            row.style.transition = 'all 0.5s ease';
            row.style.opacity = '1';
            row.style.transform = 'translateX(0)';
        }, index * 100);
    });

    // Animation des boutons de retour
    const returnButtons = document.querySelectorAll('.btn-return');
    
    returnButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const form = this.closest('form');
            
            this.style.transform = 'scale(0.95)';
            setTimeout(() => {
                form.submit();
            }, 200);
        });
    });

    // Effet de survol amélioré pour les lignes du tableau
    tableRows.forEach(row => {
        row.addEventListener('mouseenter', () => {
            row.style.transform = 'translateX(5px)';
            row.style.background = 'rgba(0, 102, 204, 0.05)';
        });

        row.addEventListener('mouseleave', () => {
            row.style.transform = 'translateX(0)';
            row.style.background = 'transparent';
        });
    });

    // Animation des alertes
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.animation = 'fadeOut 0.4s ease-out forwards';
        }, 5000);
    });

    // Mise à jour dynamique des dates restantes
    function updateRemainingDates() {
        const currentDate = new Date();
        document.querySelectorAll('td:nth-child(3)').forEach(td => {
            const dueDate = new Date(td.textContent.split('/').reverse().join('-'));
            const days = Math.ceil((dueDate - currentDate) / (1000 * 60 * 60 * 24));
            
            if (days < 0) {
                td.style.color = 'var(--danger-color)';
            } else if (days <= 3) {
                td.style.color = 'var(--warning-color)';
            }
        });
    }

    updateRemainingDates();
    setInterval(updateRemainingDates, 60000);
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