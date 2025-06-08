document.addEventListener('DOMContentLoaded', function() {
    // Animation des cartes statistiques
    const statCards = document.querySelectorAll('.stat-card');
    statCards.forEach((card, index) => {
        setTimeout(() => {
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // Initialisation des compteurs animés
    initializeCounters();

    // Gestionnaire de recherche
    initializeSearch();
});

function initializeCounters() {
    const counters = document.querySelectorAll('.stat-card p');
    counters.forEach(counter => {
        const target = parseInt(counter.innerText);
        animateCounter(counter, target);
    });
}

function animateCounter(element, target) {
    let current = 0;
    const increment = target / 30;
    const timer = setInterval(() => {
        current += increment;
        if (current >= target) {
            current = target;
            clearInterval(timer);
        }
        element.textContent = Math.round(current);
    }, 50);
}

function initializeSearch() {
    const searchInput = document.querySelector('.search-input');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(function(e) {
            // Implémentez la recherche en temps réel si nécessaire
        }, 300));
    }
}

function returnBook(id) {
    if (confirm('Êtes-vous sûr de vouloir marquer ce livre comme retourné ?')) {
        fetch(`/librarian/borrowings/${id}/return`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // Ajoutez ici les headers nécessaires pour la sécurité CSRF
            }
        })
        .then(response => {
            if (response.ok) {
                // Animation de suppression de la ligne
                const row = document.querySelector(`tr[data-borrowing-id="${id}"]`);
                if (row) {
                    row.style.animation = 'slideOut 0.5s ease-out forwards';
                    setTimeout(() => {
                        location.reload();
                    }, 500);
                } else {
                    location.reload();
                }
            } else {
                showNotification('Une erreur est survenue lors du retour du livre', 'error');
            }
        })
        .catch(error => {
            console.error('Erreur:', error);
            showNotification('Une erreur est survenue', 'error');
        });
    }
}

function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type}`;
    notification.textContent = message;
    
    const container = document.querySelector('.container');
    container.insertBefore(notification, container.firstChild);
    
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// Utilitaire pour debounce
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Animations CSS supplémentaires
const styles = `
    @keyframes slideOut {
        to {
            transform: translateX(-100%);
            opacity: 0;
        }
    }
`;

const styleSheet = document.createElement("style");
styleSheet.innerText = styles;
document.head.appendChild(styleSheet);