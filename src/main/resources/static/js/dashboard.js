document.addEventListener('DOMContentLoaded', function() {
    // Animation des statistiques
    const statBoxes = document.querySelectorAll('.stat-box');
    
    statBoxes.forEach((box, index) => {
        setTimeout(() => {
            box.style.opacity = '0';
            box.style.transform = 'translateY(20px)';
            
            requestAnimationFrame(() => {
                box.style.transition = 'all 0.5s ease';
                box.style.opacity = '1';
                box.style.transform = 'translateY(0)';
            });
        }, index * 200);
    });

    // Animation du nombre dans les statistiques
    statBoxes.forEach(box => {
        const numberElement = box.querySelector('p');
        const finalNumber = parseInt(numberElement.textContent);
        
        let currentNumber = 0;
        const duration = 2000; // 2 secondes
        const stepTime = 50;
        const steps = duration / stepTime;
        const increment = finalNumber / steps;

        const counter = setInterval(() => {
            currentNumber += increment;
            
            if (currentNumber >= finalNumber) {
                currentNumber = finalNumber;
                clearInterval(counter);
            }
            
            numberElement.textContent = Math.round(currentNumber);
        }, stepTime);
    });

    // Effet hover sur les boutons
    const buttons = document.querySelectorAll('.btn');
    
    buttons.forEach(button => {
        button.addEventListener('mousemove', (e) => {
            const rect = button.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            button.style.setProperty('--x', `${x}px`);
            button.style.setProperty('--y', `${y}px`);
        });
    });
});

// Dashboard JavaScript
class LibrarianDashboard {
    constructor() {
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.initializeCharts();
        this.startRealTimeUpdates();
        this.setupNotifications();
    }

    setupEventListeners() {
        // Sidebar toggle
        const sidebarToggle = document.querySelector('.sidebar-toggle');
        const mobileToggle = document.querySelector('.mobile-menu-toggle');
        
        if (sidebarToggle) {
            sidebarToggle.addEventListener('click', () => this.toggleSidebar());
        }
        
        if (mobileToggle) {
            mobileToggle.addEventListener('click', () => this.toggleSidebar());
        }

        // Search functionality
        const searchInput = document.querySelector('.search-container input');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => this.handleSearch(e.target.value));
        }

        // Notification click
        const notificationBtn = document.querySelector('.notification-btn');
        if (notificationBtn) {
            notificationBtn.addEventListener('click', () => this.showNotifications());
        }

        // Window resize handler
        window.addEventListener('resize', () => this.handleResize());
    }

    toggleSidebar() {
        const sidebar = document.getElementById('sidebar');
        const mainContent = document.getElementById('mainContent');
        const overlay = document.getElementById('sidebarOverlay');
        
        if (window.innerWidth <= 768) {
            sidebar.classList.toggle('collapsed');
            overlay.classList.toggle('active');
        } else {
                       sidebar.classList.toggle('collapsed');
            mainContent.classList.toggle('expanded');
        }
    }

    handleResize() {
        const sidebar = document.getElementById('sidebar');
        const mainContent = document.getElementById('mainContent');
        const overlay = document.getElementById('sidebarOverlay');
        
        if (window.innerWidth <= 768) {
            sidebar.classList.add('collapsed');
            mainContent.classList.add('expanded');
            overlay.classList.remove('active');
        } else if (window.innerWidth > 768) {
            overlay.classList.remove('active');
        }
    }

    handleSearch(query) {
        if (query.length < 2) return;
        
        // Debounce search
        clearTimeout(this.searchTimeout);
        this.searchTimeout = setTimeout(() => {
            this.performSearch(query);
        }, 300);
    }

    async performSearch(query) {
        try {
            const response = await fetch(`/api/search?q=${encodeURIComponent(query)}`);
            const results = await response.json();
            this.displaySearchResults(results);
        } catch (error) {
            console.error('Search error:', error);
        }
    }

    displaySearchResults(results) {
        // Implementation for displaying search results
        console.log('Search results:', results);
    }

    initializeCharts() {
        this.initBorrowingChart();
        this.initCategoryChart();
        this.initMemberActivityChart();
    }

    
    startRealTimeUpdates() {
        // Update time every minute
        this.updateDateTime();
        setInterval(() => this.updateDateTime(), 60000);

        // Update stats every 5 minutes
        setInterval(() => this.updateStats(), 300000);

        // Update activity feed every 2 minutes
        setInterval(() => this.updateActivityFeed(), 120000);
    }

    updateDateTime() {
        const now = new Date();
        const dateElement = document.getElementById('currentDate');
        const timeElement = document.getElementById('currentTime');
        
        if (dateElement) {
            dateElement.textContent = now.toLocaleDateString('fr-FR', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
        }
        
        if (timeElement) {
            timeElement.textContent = now.toLocaleTimeString('fr-FR', {
                hour: '2-digit',
                minute: '2-digit'
            });
        }
    }

    async updateStats() {
        try {
            const response = await fetch('/api/dashboard/stats');
            const stats = await response.json();
            this.updateStatCards(stats);
        } catch (error) {
            console.error('Error updating stats:', error);
        }
    }

    updateStatCards(stats) {
        const statElements = {
            totalBooks: document.querySelector('.stat-card.primary .stat-number'),
            borrowedBooks: document.querySelector('.stat-card.success .stat-number'),
            overdueBooks: document.querySelector('.stat-card.warning .stat-number'),
            activeMembers: document.querySelector('.stat-card.info .stat-number')
        };

        Object.keys(statElements).forEach(key => {
            if (statElements[key] && stats[key] !== undefined) {
                this.animateNumber(statElements[key], parseInt(statElements[key].textContent), stats[key]);
            }
        });
    }

    animateNumber(element, from, to) {
        const duration = 1000;
        const startTime = performance.now();
        
        const animate = (currentTime) => {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            const current = Math.floor(from + (to - from) * this.easeOutCubic(progress));
            element.textContent = current;
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        
        requestAnimationFrame(animate);
    }

    easeOutCubic(t) {
        return 1 - Math.pow(1 - t, 3);
    }

    async updateActivityFeed() {
        try {
            const response = await fetch('/api/dashboard/activity');
            const activities = await response.json();
            this.renderActivityFeed(activities);
        } catch (error) {
            console.error('Error updating activity feed:', error);
        }
    }

    renderActivityFeed(activities) {
        const activityList = document.querySelector('.activity-list');
        if (!activityList) return;

        activityList.innerHTML = activities.map(activity => `
            <div class="activity-item">
                <div class="activity-icon ${activity.type}">
                    <i class="${activity.icon}"></i>
                </div>
                <div class="activity-content">
                    <p><strong>${activity.title}</strong></p>
                    <span class="activity-time">${this.formatTimeAgo(activity.timestamp)}</span>
                </div>
            </div>
        `).join('');
    }

    formatTimeAgo(timestamp) {
        const now = new Date();
        const time = new Date(timestamp);
        const diffInSeconds = Math.floor((now - time) / 1000);

        if (diffInSeconds < 60) return 'À l\'instant';
        if (diffInSeconds < 3600) return `Il y a ${Math.floor(diffInSeconds / 60)} min`;
        if (diffInSeconds < 86400) return `Il y a ${Math.floor(diffInSeconds / 3600)} h`;
        return `Il y a ${Math.floor(diffInSeconds / 86400)} j`;
    }

    setupNotifications() {
        this.checkForNotifications();
        setInterval(() => this.checkForNotifications(), 60000);
    }

    async checkForNotifications() {
        try {
            const response = await fetch('/api/notifications');
            const notifications = await response.json();
            this.updateNotificationBadge(notifications.length);
        } catch (error) {
            console.error('Error checking notifications:', error);
        }
    }

    updateNotificationBadge(count) {
        const badge = document.querySelector('.notification-badge');
        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.style.display = 'block';
            } else {
                badge.style.display = 'none';
            }
        }
    }

    showNotifications() {
        // Use the global toggleNotificationPanel function from notifications.js
        if (window.toggleNotificationPanel) {
            window.toggleNotificationPanel();
        } else {
            console.error('toggleNotificationPanel function not available');
            // Fallback implementation
            console.log('Showing notifications...');
            // Try to fetch notifications directly
            this.fetchNotifications();
        }
    }
    
    fetchNotifications() {
        fetch('/notifications')
            .then(response => response.json())
            .then(data => {
                console.log('Notifications data:', data);
                // Update notification badge
                this.updateNotificationBadge(data.count || 0);
                
                // If we have a global update function, use it
                if (window.updateNotificationPanel) {
                    window.updateNotificationPanel(data);
                }
            })
            .catch(error => {
                console.error('Error fetching notifications:', error);
            });
    }

    // Utility methods
    showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.classList.add('show');
        }, 100);
        
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => document.body.removeChild(toast), 300);
        }, 3000);
    }

    formatNumber(num) {
        return new Intl.NumberFormat('fr-FR').format(num);
    }

    formatCurrency(amount) {
        return new Intl.NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR'
        }).format(amount);
    }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new LibrarianDashboard();
});

// Export for use in other modules
window.LibrarianDashboard = LibrarianDashboard;

