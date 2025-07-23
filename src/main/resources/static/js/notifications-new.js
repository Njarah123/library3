/**
 * Système de gestion des notifications pour le dashboard étudiant
 */

// Variables globales
let notificationPanel;
let notificationButton;
let notificationOverlay;
let notificationList;
let emptyNotification;
let loadingNotification;
let errorNotification;
let notificationsData = [];
let unreadCount = 0;

// Initialisation du système de notifications
document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing notification system...');
    
    // Attendre un court délai pour s'assurer que tous les éléments sont chargés
    setTimeout(function() {
        // Récupération des éléments DOM
        notificationPanel = document.getElementById('notificationPanel');
        notificationButton = document.getElementById('notificationButton');
        notificationOverlay = document.getElementById('notificationOverlay');
        notificationList = document.getElementById('notificationList');
        emptyNotification = document.getElementById('emptyNotification');
        loadingNotification = document.getElementById('loadingNotification');
        errorNotification = document.getElementById('errorNotification');
        
        console.log('Notification panel found:', !!notificationPanel);
        console.log('Notification button found:', !!notificationButton);
        
        if (!notificationPanel) {
            console.error('Élément notificationPanel non trouvé dans le DOM');
        }
        
        if (!notificationButton) {
            console.error('Élément notificationButton non trouvé dans le DOM');
            
            // Essayer de trouver le bouton par sa classe
            const buttons = document.querySelectorAll('.notification-btn');
            if (buttons.length > 0) {
                notificationButton = buttons[0];
                console.log('Notification button found by class');
            }
        }
        
        if (notificationPanel && notificationButton) {
            // Initialisation des événements
            initNotificationEvents();
            
            // Chargement initial des notifications
            fetchNotifications();
            
            // Mise à jour périodique des notifications (toutes les 60 secondes)
            setInterval(fetchNotifications, 60000);
            
            console.log('Notification system initialized successfully');
        } else {
            console.error('Impossible d\'initialiser le système de notifications');
        }
    }, 500); // Attendre 500ms pour s'assurer que le DOM est complètement chargé
});

// Initialisation des événements pour les notifications
function initNotificationEvents() {
    console.log('Initializing notification events');
    
    // Ouverture/fermeture du panneau de notification
    notificationButton.addEventListener('click', function(e) {
        console.log('Notification button clicked');
        e.preventDefault();
        e.stopPropagation();
        toggleNotificationPanel();
    });
    
    // Fermeture du panneau lors du clic sur l'overlay
    if (notificationOverlay) {
        notificationOverlay.addEventListener('click', function() {
            closeNotificationPanel();
        });
    }
    
    // Fermeture du panneau lors du clic en dehors (sauf sur les éléments avec data-no-close)
    document.addEventListener('click', function(e) {
        if (notificationPanel && notificationPanel.classList.contains('show')) {
            // Vérifier si l'élément ou un de ses parents a l'attribut data-no-close
            let target = e.target;
            let shouldClose = true;
            
            while (target != null) {
                if (target.hasAttribute && target.hasAttribute('data-no-close')) {
                    shouldClose = false;
                    break;
                }
                target = target.parentElement;
            }
            
            if (shouldClose) {
                closeNotificationPanel();
            }
        }
    });
    
    console.log('Notification events initialized');
}

// Ouverture/fermeture du panneau de notification
function toggleNotificationPanel() {
    console.log('Toggle notification panel called');
    
    if (!notificationPanel) {
        console.error('Notification panel not found');
        return;
    }
    
    if (notificationPanel.classList.contains('show')) {
        closeNotificationPanel();
    } else {
        openNotificationPanel();
    }
}

// Expose the toggleNotificationPanel function to the global scope
window.toggleNotificationPanel = toggleNotificationPanel;

// Ouverture du panneau de notification
function openNotificationPanel() {
    console.log('Opening notification panel');
    
    if (!notificationPanel) {
        console.error('Cannot open notification panel: element not found');
        return;
    }
    
    notificationPanel.classList.add('show');
    
    if (notificationOverlay) {
        notificationOverlay.classList.add('show');
    }
    
    // Marquer les notifications comme lues après un délai
    setTimeout(function() {
        markNotificationsAsRead();
    }, 2000);
}

// Fermeture du panneau de notification
function closeNotificationPanel() {
    console.log('Closing notification panel');
    
    if (!notificationPanel) {
        console.error('Cannot close notification panel: element not found');
        return;
    }
    
    notificationPanel.classList.remove('show');
    
    if (notificationOverlay) {
        notificationOverlay.classList.remove('show');
    }
}

// Récupération des notifications depuis le serveur
function fetchNotifications() {
    console.log('Fetching notifications');
    
    if (!notificationList || !loadingNotification) {
        console.error('Cannot fetch notifications: required elements not found');
        return;
    }
    
    // Afficher l'indicateur de chargement
    showLoadingState();
    
    try {
        // Récupération des notifications via une requête AJAX
        const tokenElement = document.querySelector('meta[name="_csrf"]');
        const headerElement = document.querySelector('meta[name="_csrf_header"]');
        
        if (!tokenElement || !headerElement) {
            console.error('CSRF tokens not found');
            showErrorState();
            return;
        }
        
        const token = tokenElement.getAttribute('content');
        const header = headerElement.getAttribute('content');
        
        fetch('/api/notifications', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            },
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Erreur lors de la récupération des notifications');
            }
            return response.json();
        })
        .then(data => {
            // Mise à jour des données de notification
            notificationsData = data;
            
            // Mise à jour de l'affichage
            updateNotificationDisplay();
        })
        .catch(error => {
            console.error('Erreur:', error);
            showErrorState();
        });
    } catch (error) {
        console.error('Exception lors de la récupération des notifications:', error);
        showErrorState();
    }
}

// Mise à jour de l'affichage des notifications
function updateNotificationDisplay() {
    // Masquer les états de chargement et d'erreur
    loadingNotification.style.display = 'none';
    errorNotification.style.display = 'none';
    
    // Vider la liste des notifications
    notificationList.innerHTML = '';
    
    // Vérifier s'il y a des notifications
    if (notificationsData.length === 0) {
        emptyNotification.style.display = 'block';
        return;
    }
    
    // Masquer le message "aucune notification"
    emptyNotification.style.display = 'none';
    
    // Compter les notifications non lues
    unreadCount = notificationsData.filter(notification => !notification.read).length;
    
    // Mettre à jour le badge de notification
    updateNotificationBadge();
    
    // Ajouter chaque notification à la liste
    notificationsData.forEach(notification => {
        const notificationItem = createNotificationItem(notification);
        notificationList.appendChild(notificationItem);
    });
}

// Création d'un élément de notification
function createNotificationItem(notification) {
    const notificationItem = document.createElement('div');
    notificationItem.className = 'notification-item';
    if (!notification.read) {
        notificationItem.classList.add('unread');
    }
    notificationItem.setAttribute('data-id', notification.id);
    notificationItem.setAttribute('data-no-close', 'true');
    
    // Déterminer l'icône en fonction du type de notification
    let iconClass = 'fas fa-bell';
    let iconType = '';
    
    switch (notification.type) {
        case 'WARNING':
            iconClass = 'fas fa-exclamation-triangle';
            iconType = 'warning';
            break;
        case 'DANGER':
            iconClass = 'fas fa-exclamation-circle';
            iconType = 'danger';
            break;
        case 'SUCCESS':
            iconClass = 'fas fa-check-circle';
            iconType = 'success';
            break;
        default:
            iconClass = 'fas fa-bell';
            iconType = '';
    }
    
    // Calculer le temps écoulé
    const timeAgo = getTimeAgo(new Date(notification.createdAt));
    
    // Construire le HTML de la notification
    notificationItem.innerHTML = `
        <div class="notification-icon ${iconType}" data-no-close="true">
            <i class="${iconClass}" data-no-close="true"></i>
        </div>
        <div class="notification-content" data-no-close="true">
            <h6 class="notification-title" data-no-close="true">${notification.title}</h6>
            <p class="notification-message" data-no-close="true">${notification.message}</p>
            <div class="notification-time" data-no-close="true">
                <i class="fas fa-clock" data-no-close="true"></i>
                <span data-no-close="true">${timeAgo}</span>
            </div>
            ${notification.actionUrl ? `
            <div class="notification-actions" data-no-close="true">
                <a href="${notification.actionUrl}" class="notification-action-btn" data-no-close="true">
                    ${notification.actionText || 'Voir plus'}
                </a>
            </div>
            ` : ''}
        </div>
    `;
    
    // Ajouter un événement de clic pour marquer comme lu
    notificationItem.addEventListener('click', function() {
        markNotificationAsRead(notification.id);
        
        // Rediriger si une URL d'action est spécifiée
        if (notification.actionUrl) {
            window.location.href = notification.actionUrl;
        }
    });
    
    return notificationItem;
}

// Calcul du temps écoulé depuis une date
function getTimeAgo(date) {
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) {
        return 'À l\'instant';
    }
    
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) {
        return `Il y a ${diffInMinutes} minute${diffInMinutes > 1 ? 's' : ''}`;
    }
    
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) {
        return `Il y a ${diffInHours} heure${diffInHours > 1 ? 's' : ''}`;
    }
    
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 30) {
        return `Il y a ${diffInDays} jour${diffInDays > 1 ? 's' : ''}`;
    }
    
    const diffInMonths = Math.floor(diffInDays / 30);
    if (diffInMonths < 12) {
        return `Il y a ${diffInMonths} mois`;
    }
    
    const diffInYears = Math.floor(diffInMonths / 12);
    return `Il y a ${diffInYears} an${diffInYears > 1 ? 's' : ''}`;
}

// Marquer une notification comme lue
function markNotificationAsRead(notificationId) {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    fetch(`/api/notifications/${notificationId}/read`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        credentials: 'same-origin'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erreur lors du marquage de la notification comme lue');
        }
        
        // Mettre à jour l'état local
        const notification = notificationsData.find(n => n.id === notificationId);
        if (notification) {
            notification.read = true;
            
            // Mettre à jour l'affichage
            const notificationItem = document.querySelector(`.notification-item[data-id="${notificationId}"]`);
            if (notificationItem) {
                notificationItem.classList.remove('unread');
            }
            
            // Mettre à jour le compteur
            unreadCount = notificationsData.filter(n => !n.read).length;
            updateNotificationBadge();
        }
    })
    .catch(error => {
        console.error('Erreur:', error);
    });
}

// Marquer toutes les notifications comme lues
function markAllNotificationsAsRead(event) {
    if (event) {
        event.preventDefault();
        event.stopPropagation();
    }
    
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    
    fetch('/api/notifications/read-all', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        },
        credentials: 'same-origin'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Erreur lors du marquage de toutes les notifications comme lues');
        }
        
        // Mettre à jour l'état local
        notificationsData.forEach(notification => {
            notification.read = true;
        });
        
        // Mettre à jour l'affichage
        document.querySelectorAll('.notification-item').forEach(item => {
            item.classList.remove('unread');
        });
        
        // Mettre à jour le compteur
        unreadCount = 0;
        updateNotificationBadge();
    })
    .catch(error => {
        console.error('Erreur:', error);
    });
}

// Mise à jour du badge de notification
function updateNotificationBadge() {
    const badge = document.querySelector('.notification-btn .notification-badge');
    
    if (badge) {
        if (unreadCount > 0) {
            badge.textContent = unreadCount;
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    }
}

// Afficher l'état de chargement
function showLoadingState() {
    notificationList.innerHTML = '';
    emptyNotification.style.display = 'none';
    errorNotification.style.display = 'none';
    loadingNotification.style.display = 'block';
}

// Afficher l'état d'erreur
function showErrorState() {
    notificationList.innerHTML = '';
    emptyNotification.style.display = 'none';
    loadingNotification.style.display = 'none';
    errorNotification.style.display = 'block';
}

// Marquer les notifications comme lues lorsqu'elles sont visibles
function markNotificationsAsRead() {
    // Ne marquer comme lues que si le panneau est ouvert
    if (!notificationPanel.classList.contains('show')) {
        return;
    }
    
    // Récupérer les notifications non lues visibles
    const unreadNotifications = notificationsData.filter(notification => !notification.read);
    
    // Si aucune notification non lue, ne rien faire
    if (unreadNotifications.length === 0) {
        return;
    }
    
    // Marquer chaque notification comme lue
    unreadNotifications.forEach(notification => {
        markNotificationAsRead(notification.id);
    });
}