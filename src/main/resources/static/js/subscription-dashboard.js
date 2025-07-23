/**
 * JavaScript pour le tableau de bord de gestion des abonnements
 */
document.addEventListener('DOMContentLoaded', function() {
    // Initialisation des tooltips Bootstrap
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Gestion des filtres pour l'historique des abonnements
    const subscriptionFilterButtons = document.querySelectorAll('.history-section:nth-of-type(1) .filter-btn');
    subscriptionFilterButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Supprimer la classe active de tous les boutons
            subscriptionFilterButtons.forEach(btn => btn.classList.remove('active'));
            
            // Ajouter la classe active au bouton cliqué
            this.classList.add('active');
            
            // Récupérer la valeur du filtre
            const filter = this.getAttribute('data-filter');
            
            // Filtrer les lignes du tableau
            const rows = document.querySelectorAll('.history-section:nth-of-type(1) tbody tr');
            rows.forEach(row => {
                if (filter === 'all') {
                    row.style.display = '';
                } else {
                    const rowStatus = row.getAttribute('data-status');
                    
                    if (rowStatus === filter) {
                        row.style.display = '';
                    } else {
                        row.style.display = 'none';
                    }
                }
            });
        });
    });

    // Gestion des filtres pour l'historique des paiements
    const paymentFilterButtons = document.querySelectorAll('.history-section:nth-of-type(2) .filter-btn');
    paymentFilterButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Supprimer la classe active de tous les boutons
            paymentFilterButtons.forEach(btn => btn.classList.remove('active'));
            
            // Ajouter la classe active au bouton cliqué
            this.classList.add('active');
            
            // Récupérer la valeur du filtre
            const filter = this.getAttribute('data-filter');
            
            // Filtrer les lignes du tableau
            const rows = document.querySelectorAll('.history-section:nth-of-type(2) tbody tr');
            rows.forEach(row => {
                if (filter === 'all') {
                    row.style.display = '';
                } else {
                    const rowType = row.getAttribute('data-type');
                    
                    if (rowType === filter) {
                        row.style.display = '';
                    } else {
                        row.style.display = 'none';
                    }
                }
            });
        });
    });

    // Initialisation du graphique d'utilisation si le canvas existe
    const ctx = document.getElementById('usageChart');
    if (ctx) {
        // Récupérer les données du graphique depuis l'attribut data
        const chartData = JSON.parse(ctx.getAttribute('data-chart') || '[]');
        const chartLabels = JSON.parse(ctx.getAttribute('data-labels') || '[]');
        
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: chartLabels.length > 0 ? chartLabels : ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc'],
                datasets: [{
                    label: 'Livres empruntés',
                    data: chartData.length > 0 ? chartData : [2, 3, 5, 4, 6, 8, 7, 9, 10, 8, 7, 5],
                    borderColor: '#4f46e5',
                    backgroundColor: 'rgba(79, 70, 229, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            precision: 0
                        }
                    }
                }
            }
        });
    }

    // Gestion de la modal de confirmation d'annulation d'abonnement
    const cancelSubscriptionModal = document.getElementById('cancelSubscriptionModal');
    if (cancelSubscriptionModal) {
        cancelSubscriptionModal.addEventListener('show.bs.modal', function (event) {
            // Bouton qui a déclenché la modal
            const button = event.relatedTarget;
            
            // Récupérer les informations de l'abonnement si nécessaire
            const subscriptionId = button.getAttribute('data-subscription-id');
            const subscriptionType = button.getAttribute('data-subscription-type');
            
            // Mettre à jour le contenu de la modal si nécessaire
            const modalTitle = cancelSubscriptionModal.querySelector('.modal-title');
            const modalBody = cancelSubscriptionModal.querySelector('.modal-body p');
            
            if (subscriptionType) {
                modalTitle.textContent = `Annuler l'abonnement ${subscriptionType}`;
                modalBody.textContent = `Cette action ne peut pas être annulée. Votre abonnement ${subscriptionType} restera actif jusqu'à la fin de la période en cours.`;
            }
            
            // Mettre à jour le formulaire si nécessaire
            const form = cancelSubscriptionModal.querySelector('form');
            if (subscriptionId && form) {
                form.action = `/subscriptions/cancel/${subscriptionId}`;
            }
        });
    }
});