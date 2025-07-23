/**
 * NOTIFICATION PANEL ULTRA - JavaScript Ultra-Efficace
 * Système de notification moderne avec gestion d'état optimisée
 */

class NotificationPanelUltra {
    constructor(buttonId = 'notificationBtn') {
        // Configuration
        this.config = {
            apiBaseUrl: '/api/notifications',
            pollInterval: 30000, // 30 secondes
            maxRetries: 3,
            retryDelay: 2000,
            animationDuration: 300,
            maxNotifications: 50
        };
        
        // ID du bouton personnalisé
        this.buttonId = buttonId;

        // État du panneau
        this.state = {
            isOpen: false,
            isLoading: false,
            notifications: [],
            unreadCount: 0,
            lastUpdate: null,
            retryCount: 0
        };

        // Éléments DOM
        this.elements = {};
        
        // Timers
        this.pollTimer = null;
        this.retryTimer = null;

        // Initialisation
        this.init();
    }

    /**
     * Initialisation du système
     */
    async init() {
        try {
            console.log('🚀 Initialisation du système de notification ultra...');
            
            // Attendre que le DOM soit prêt
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', () => this.setup());
            } else {
                this.setup();
            }
        } catch (error) {
            console.error('❌ Erreur lors de l\'initialisation:', error);
        }
    }

    /**
     * Configuration des éléments et événements
     */
    setup() {
        try {
            // Récupération des éléments DOM
            this.getElements();
            
            // Vérification des éléments requis
            if (!this.elements.btn || !this.elements.panel) {
                console.warn('⚠️ Éléments de notification non trouvés');
                return;
            }

            // Configuration des événements
            this.setupEvents();
            
            // Chargement initial
            this.loadNotifications();
            
            // Démarrage du polling
            this.startPolling();
            
            console.log('✅ Système de notification ultra initialisé avec succès');
        } catch (error) {
            console.error('❌ Erreur lors de la configuration:', error);
        }
    }

    /**
     * Récupération des éléments DOM
     */
    getElements() {
        this.elements = {
            btn: document.getElementById(this.buttonId),
            badge: document.getElementById(this.buttonId)?.querySelector('.notification-badge'),
            panel: document.getElementById('notificationPanel'),
            overlay: document.getElementById('notificationOverlay'),
            closeBtn: document.getElementById('closeNotificationBtn'),
            markAllBtn: document.getElementById('markAllReadBtn'),
            retryBtn: document.getElementById('retryBtn'),
            viewAllBtn: document.getElementById('viewAllBtn'),
            
            // États
            loadingState: document.getElementById('loadingState'),
            emptyState: document.getElementById('emptyState'),
            errorState: document.getElementById('errorState'),
            notificationList: document.getElementById('notificationList'),
            
            // Template
            template: document.getElementById('notificationTemplate')
        };
    }

    /**
     * Configuration des événements
     */
    setupEvents() {
        // Bouton principal
        this.elements.btn?.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.toggle();
        });

        // Bouton fermer
        this.elements.closeBtn?.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.close();
        });

        // Marquer tout comme lu
        this.elements.markAllBtn?.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.markAllAsRead();
        });

        // Bouton réessayer
        this.elements.retryBtn?.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            this.loadNotifications();
        });

        // Overlay
        this.elements.overlay?.addEventListener('click', () => {
            this.close();
        });

        // Fermeture avec Escape
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.state.isOpen) {
                this.close();
            }
        });

        // Fermeture en cliquant à l'extérieur
        document.addEventListener('click', (e) => {
            if (this.state.isOpen && !this.elements.panel?.contains(e.target) && !this.elements.btn?.contains(e.target)) {
                this.close();
            }
        });

        // Gestion de la visibilité de la page
        document.addEventListener('visibilitychange', () => {
            if (!document.hidden && this.state.isOpen) {
                this.loadNotifications();
            }
        });
    }

    /**
     * Ouverture/fermeture du panneau
     */
    toggle() {
        if (this.state.isOpen) {
            this.close();
        } else {
            this.open();
        }
    }

    /**
     * Ouverture du panneau
     */
    async open() {
        if (this.state.isOpen) return;

        try {
            this.state.isOpen = true;
            
            // Animation d'ouverture
            this.elements.panel?.classList.add('show');
            this.elements.overlay?.classList.add('show');
            this.elements.btn?.classList.add('active');
            
            // Chargement des notifications
            await this.loadNotifications();
            
            // Marquer les notifications comme vues après un délai
            setTimeout(() => {
                if (this.state.isOpen) {
                    this.markNotificationsAsSeen();
                }
            }, 2000);
            
        } catch (error) {
            console.error('❌ Erreur lors de l\'ouverture:', error);
        }
    }

    /**
     * Fermeture du panneau
     */
    close() {
        if (!this.state.isOpen) return;

        this.state.isOpen = false;
        
        // Animation de fermeture
        this.elements.panel?.classList.remove('show');
        this.elements.overlay?.classList.remove('show');
        this.elements.btn?.classList.remove('active');
    }

    /**
     * Chargement des notifications
     */
    async loadNotifications() {
        if (this.state.isLoading) return;

        try {
            console.log('🔄 Chargement des notifications...');
            this.state.isLoading = true;
            this.showLoadingState();
            
            const response = await this.apiCall('GET', '');
            console.log('📡 Réponse API reçue:', response);
            
            if (response.success) {
                this.state.notifications = response.notifications || [];
                this.state.unreadCount = response.unreadCount || 0;
                this.state.lastUpdate = Date.now();
                this.state.retryCount = 0;
                
                console.log('✅ Notifications chargées:', this.state.notifications.length, 'notifications, dont', this.state.unreadCount, 'non lues');
                
                this.updateUI();
                this.showNotificationList();
            } else {
                console.error('❌ Erreur dans la réponse API:', response.message);
                throw new Error(response.message || 'Erreur de chargement');
            }
            
        } catch (error) {
            console.error('❌ Erreur lors du chargement:', error);
            this.handleError(error);
        } finally {
            this.state.isLoading = false;
        }
    }

    /**
     * Marquer une notification comme lue
     */
    async markAsRead(notificationId) {
        try {
            const response = await this.apiCall('POST', `/${notificationId}/read`);
            
            if (response.success) {
                // Mettre à jour l'état local
                const notification = this.state.notifications.find(n => n.id === notificationId);
                if (notification && !notification.read) {
                    notification.read = true;
                    this.state.unreadCount = Math.max(0, this.state.unreadCount - 1);
                    this.updateUI();
                }
            }
            
            return response.success;
        } catch (error) {
            console.error('❌ Erreur lors du marquage:', error);
            return false;
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    async markAllAsRead() {
        try {
            const response = await this.apiCall('POST', '/read-all');
            
            if (response.success) {
                // Mettre à jour l'état local
                this.state.notifications.forEach(n => n.read = true);
                this.state.unreadCount = 0;
                this.updateUI();
                this.renderNotifications();
            }
            
            return response.success;
        } catch (error) {
            console.error('❌ Erreur lors du marquage global:', error);
            return false;
        }
    }

    /**
     * Supprimer une notification
     */
    async deleteNotification(notificationId) {
        try {
            const response = await this.apiCall('DELETE', `/${notificationId}`);
            
            if (response.success) {
                // Retirer de l'état local
                const index = this.state.notifications.findIndex(n => n.id === notificationId);
                if (index !== -1) {
                    const notification = this.state.notifications[index];
                    if (!notification.read) {
                        this.state.unreadCount = Math.max(0, this.state.unreadCount - 1);
                    }
                    this.state.notifications.splice(index, 1);
                    this.updateUI();
                    this.renderNotifications();
                }
            }
            
            return response.success;
        } catch (error) {
            console.error('❌ Erreur lors de la suppression:', error);
            return false;
        }
    }

    /**
     * Marquer les notifications comme vues (réduire le badge)
     */
    markNotificationsAsSeen() {
        // Cette fonction peut être utilisée pour marquer les notifications comme "vues"
        // sans les marquer comme "lues"
        if (this.state.unreadCount > 0) {
            // Optionnel: réduire visuellement le badge sans marquer comme lu
        }
    }

    /**
     * Appel API générique
     */
    async apiCall(method, endpoint, data = null) {
        const url = `${this.config.apiBaseUrl}${endpoint}`;
        
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'same-origin'
        };

        // Ajouter le token CSRF
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        if (csrfToken && csrfHeader) {
            options.headers[csrfHeader] = csrfToken;
        }

        if (data && (method === 'POST' || method === 'PUT')) {
            options.body = JSON.stringify(data);
        }

        try {
            console.log('🌐 Appel API:', method, url, options);
            const response = await fetch(url, options);
            console.log('📡 Réponse HTTP:', response.status, response.statusText);
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            
            // Récupérer le texte brut d'abord
            const responseText = await response.text();
            console.log('📄 Réponse brute reçue:', responseText);
            
            // Parser le JSON manuellement
            let result;
            try {
                result = JSON.parse(responseText);
                console.log('✅ JSON parsé avec succès:', result);
            } catch (parseError) {
                console.error('❌ Erreur de parsing JSON:', parseError);
                console.log('📄 Contenu qui a causé l\'erreur:', responseText);
                throw new Error('Réponse JSON invalide: ' + parseError.message);
            }
            
            // Vérifier que c'est un objet valide
            if (typeof result !== 'object' || result === null) {
                console.error('❌ La réponse n\'est pas un objet JSON valide:', typeof result, result);
                throw new Error('Réponse invalide: pas un objet JSON');
            }
            
            // Adapter la structure de réponse du contrôleur
            const adaptedResult = {
                success: result.success !== false,
                notifications: Array.isArray(result.notifications) ? result.notifications : [],
                unreadCount: typeof result.unreadCount === 'number' ? result.unreadCount : 0,
                message: result.message || '',
                timestamp: result.timestamp || Date.now()
            };
            
            console.log('🔄 Résultat adapté:', adaptedResult);
            console.log('📊 Notifications trouvées:', adaptedResult.notifications.length);
            console.log('🔢 Notifications non lues:', adaptedResult.unreadCount);
            
            return adaptedResult;
        } catch (error) {
            console.error('❌ Erreur API complète:', error);
            return {
                success: false,
                message: error.message,
                notifications: [],
                unreadCount: 0
            };
        }
    }

    /**
     * Mise à jour de l'interface utilisateur
     */
    updateUI() {
        this.updateBadge();
        this.renderNotifications();
    }

    /**
     * Mise à jour du badge
     */
    updateBadge() {
        if (!this.elements.badge) return;

        if (this.state.unreadCount > 0) {
            this.elements.badge.textContent = this.state.unreadCount > 99 ? '99+' : this.state.unreadCount;
            this.elements.badge.style.display = 'flex';
        } else {
            this.elements.badge.style.display = 'none';
        }
    }

    /**
     * Rendu des notifications
     */
    renderNotifications() {
        console.log('🎨 Rendu des notifications:', this.state.notifications.length, 'notifications');
        
        if (!this.elements.notificationList) {
            console.error('❌ notificationList manquant');
            return;
        }
        
        // Créer le template dynamiquement si il n'existe pas
        if (!this.elements.template) {
            this.createNotificationTemplate();
        }

        // Vider la liste
        this.elements.notificationList.innerHTML = '';

        if (this.state.notifications.length === 0) {
            this.showEmptyState();
            return;
        }

        // Créer les éléments de notification
        this.state.notifications.forEach((notification, index) => {
            try {
                const element = this.createNotificationElement(notification);
                if (element) {
                    this.elements.notificationList.appendChild(element);
                } else {
                    console.error(`❌ Échec de création de l'élément pour la notification ${index + 1}`);
                }
            } catch (error) {
                console.error(`❌ Erreur lors du rendu de la notification ${index + 1}:`, error);
            }
        });
        
        console.log('✅ Rendu terminé avec succès');
    }

    /**
     * Création du template de notification dynamiquement
     */
    createNotificationTemplate() {
        console.log('🔧 Création du template de notification');
        
        // Créer l'élément template
        const template = document.createElement('template');
        template.id = 'notificationTemplate';
        
        // Définir le contenu HTML du template avec photo de profil
        template.innerHTML = `
            <div class="notification-item" data-id="">
                <div class="notification-profile">
                    <img class="notification-profile-image" src="" alt="Profile">
                    <div class="notification-icon">
                        <i class="fas fa-bell"></i>
                    </div>
                </div>
                <div class="notification-body">
                    <div class="notification-header-item">
                        <h5 class="notification-title-item"></h5>
                        <span class="notification-time"></span>
                    </div>
                    <p class="notification-message"></p>
                    <div class="notification-actions-item">
                        <button class="btn-mark-read" title="Marquer comme lu">
                            <i class="fas fa-check"></i>
                        </button>
                        <button class="btn-delete" title="Supprimer">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
                <div class="notification-indicator"></div>
            </div>
        `;
        
        // Ajouter le template au DOM
        document.body.appendChild(template);
        
        // Mettre à jour la référence
        this.elements.template = template;
        
        console.log('✅ Template créé avec succès');
    }

    /**
     * Création d'un élément de notification
     */
    createNotificationElement(notification) {
        if (!this.elements.template) {
            console.error('❌ Template manquant');
            return null;
        }
        
        const template = this.elements.template.content.cloneNode(true);
        const element = template.querySelector('.notification-item');
        
        if (!element) {
            console.error('❌ Élément .notification-item non trouvé dans le template');
            return null;
        }
        
        // Configuration de l'élément
        element.setAttribute('data-id', notification.id);
        
        // Classes CSS
        if (!notification.read) {
            element.classList.add('unread');
        }
        
        if (notification.type) {
            element.classList.add(`type-${notification.type.toLowerCase()}`);
        }

        // Photo de profil de l'utilisateur déclencheur
        const profileImage = element.querySelector('.notification-profile-image');
        if (profileImage) {
            // Gérer l'erreur de chargement d'image une seule fois
            profileImage.addEventListener('error', function() {
                if (!this.hasAttribute('data-fallback-attempted')) {
                    this.setAttribute('data-fallback-attempted', 'true');
                    if (notification.triggeredByUser) {
                        // Essayer l'image par défaut selon le type d'utilisateur
                        const userType = notification.triggeredByUser.userType || 'USER';
                        if (userType === 'LIBRARIAN') {
                            this.src = '/images/default-librarian.png';
                        } else if (userType === 'STAFF') {
                            this.src = '/images/default-staff.png';
                        } else {
                            this.src = '/images/default-student.png';
                        }
                    } else {
                        // Utiliser l'image système
                        this.src = '/images/default-system.png';
                    }
                } else {
                    // Si même l'image de fallback échoue, masquer l'image
                    this.style.display = 'none';
                    console.warn('⚠️ Impossible de charger l\'image de profil pour la notification', notification.id);
                }
            });

            if (notification.triggeredByUser) {
                const triggeredBy = notification.triggeredByUser;
                console.log('🖼️ DEBUG: Données utilisateur déclencheur:', triggeredBy);
                console.log('🖼️ DEBUG: profileImagePath:', triggeredBy.profileImagePath);
                console.log('🖼️ DEBUG: userType:', triggeredBy.userType);
                
                // Utiliser le chemin de profil ou l'image par défaut selon le type
                if (triggeredBy.profileImagePath && triggeredBy.profileImagePath.trim() !== '') {
                    profileImage.src = triggeredBy.profileImagePath;
                    console.log('🖼️ DEBUG: Utilisation du chemin de profil:', triggeredBy.profileImagePath);
                } else {
                    // Image par défaut selon le type d'utilisateur
                    const userType = triggeredBy.userType || 'USER';
                    let defaultImageSrc = '';
                    if (userType === 'LIBRARIAN') {
                        defaultImageSrc = '/images/default-librarian.png';
                    } else if (userType === 'STAFF') {
                        defaultImageSrc = '/images/default-staff.png';
                    } else {
                        defaultImageSrc = '/images/default-student.png';
                    }
                    profileImage.src = defaultImageSrc;
                    console.log('🖼️ DEBUG: Utilisation de l\'image par défaut:', defaultImageSrc, 'pour le type:', userType);
                }
                profileImage.alt = `Photo de ${triggeredBy.name}`;
                profileImage.title = `Déclenché par ${triggeredBy.name}`;
                console.log('🖼️ DEBUG: Image finale définie:', profileImage.src);
            } else {
                // Pas d'utilisateur déclencheur, utiliser une image par défaut
                profileImage.src = '/images/default-system.png';
                profileImage.alt = 'Notification système';
                profileImage.title = 'Notification système';
                console.log('🖼️ DEBUG: Notification système, image:', profileImage.src);
            }
        }

        // Icône
        const icon = element.querySelector('.notification-icon i');
        if (icon) {
            icon.className = this.getNotificationIcon(notification.type);
        }

        // Titre
        const title = element.querySelector('.notification-title-item');
        if (title) {
            title.textContent = notification.title || 'Notification';
        }

        // Message
        const message = element.querySelector('.notification-message');
        if (message) {
            message.textContent = notification.message || '';
        }

        // Temps
        const time = element.querySelector('.notification-time');
        if (time) {
            time.textContent = this.formatTime(notification.createdAt);
        }

        // Événements
        this.setupNotificationEvents(element, notification);

        return element;
    }

    /**
     * Configuration des événements d'une notification
     */
    setupNotificationEvents(element, notification) {
        // Clic sur la notification
        element.addEventListener('click', (e) => {
            if (!e.target.closest('.notification-actions-item')) {
                this.markAsRead(notification.id);
            }
        });

        // Bouton marquer comme lu
        const markReadBtn = element.querySelector('.btn-mark-read');
        markReadBtn?.addEventListener('click', (e) => {
            e.stopPropagation();
            this.markAsRead(notification.id);
        });

        // Bouton supprimer
        const deleteBtn = element.querySelector('.btn-delete');
        deleteBtn?.addEventListener('click', (e) => {
            e.stopPropagation();
            this.deleteNotification(notification.id);
        });
    }

    /**
     * Obtenir l'icône pour un type de notification
     */
    getNotificationIcon(type) {
        const icons = {
            'SUCCESS': 'fas fa-check-circle',
            'WARNING': 'fas fa-exclamation-triangle',
            'DANGER': 'fas fa-exclamation-circle',
            'INFO': 'fas fa-info-circle',
            'BOOK_BORROWED': 'fas fa-book-reader',
            'BOOK_RETURNED': 'fas fa-book',
            'NEW_BOOK_ADDED': 'fas fa-plus-circle',
            'OVERDUE': 'fas fa-clock'
        };
        
        return icons[type] || 'fas fa-bell';
    }

    /**
     * Formatage du temps
     */
    formatTime(dateString) {
        if (!dateString) return '';
        
        const date = new Date(dateString);
        const now = new Date();
        const diffMs = now - date;
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMins / 60);
        const diffDays = Math.floor(diffHours / 24);

        if (diffMins < 1) return 'À l\'instant';
        if (diffMins < 60) return `Il y a ${diffMins}min`;
        if (diffHours < 24) return `Il y a ${diffHours}h`;
        if (diffDays < 7) return `Il y a ${diffDays}j`;
        
        return date.toLocaleDateString('fr-FR', {
            day: 'numeric',
            month: 'short'
        });
    }

    /**
     * Affichage des différents états
     */
    showLoadingState() {
        this.hideAllStates();
        this.elements.loadingState?.style.setProperty('display', 'flex');
    }

    showEmptyState() {
        this.hideAllStates();
        this.elements.emptyState?.style.setProperty('display', 'flex');
    }

    showErrorState() {
        this.hideAllStates();
        this.elements.errorState?.style.setProperty('display', 'flex');
    }

    showNotificationList() {
        this.hideAllStates();
        this.elements.notificationList?.style.setProperty('display', 'block');
    }

    hideAllStates() {
        this.elements.loadingState?.style.setProperty('display', 'none');
        this.elements.emptyState?.style.setProperty('display', 'none');
        this.elements.errorState?.style.setProperty('display', 'none');
        this.elements.notificationList?.style.setProperty('display', 'none');
    }

    /**
     * Gestion des erreurs
     */
    handleError(error) {
        console.error('❌ Erreur de notification:', error);
        
        this.state.retryCount++;
        
        if (this.state.retryCount < this.config.maxRetries) {
            // Retry automatique
            this.retryTimer = setTimeout(() => {
                this.loadNotifications();
            }, this.config.retryDelay * this.state.retryCount);
        } else {
            // Afficher l'état d'erreur
            this.showErrorState();
        }
    }

    /**
     * Démarrage du polling
     */
    startPolling() {
        this.stopPolling();
        
        this.pollTimer = setInterval(() => {
            if (!this.state.isLoading) {
                this.checkForUpdates();
            }
        }, this.config.pollInterval);
    }

    /**
     * Arrêt du polling
     */
    stopPolling() {
        if (this.pollTimer) {
            clearInterval(this.pollTimer);
            this.pollTimer = null;
        }
        
        if (this.retryTimer) {
            clearTimeout(this.retryTimer);
            this.retryTimer = null;
        }
    }

    /**
     * Vérification des mises à jour
     */
    async checkForUpdates() {
        try {
            const response = await this.apiCall('GET', '/unread-count');
            
            if (response.success && (response.count || response.unreadCount) !== this.state.unreadCount) {
                // Il y a des changements, recharger
                await this.loadNotifications();
            }
        } catch (error) {
            // Erreur silencieuse pour le polling
            console.warn('⚠️ Erreur lors de la vérification:', error);
        }
    }

    /**
     * Nettoyage
     */
    destroy() {
        this.stopPolling();
        this.close();
        
        // Nettoyer les événements si nécessaire
        console.log('🧹 Système de notification nettoyé');
    }
}

// Export pour utilisation externe
window.NotificationPanelUltra = NotificationPanelUltra;