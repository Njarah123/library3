class SessionManager {
    constructor(timeoutMinutes = 30) {
        this.timeoutDuration = timeoutMinutes * 60 * 1000;
        this.timeoutId = null;
        this.lastActivity = Date.now();
        this.isWarningShown = false;
        
        this.init();
    }
    
    init() {
        const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];
        
        events.forEach(event => {
            document.addEventListener(event, () => {
                this.resetTimer();
            }, true);
        });
        
        this.resetTimer();
    }
    
    resetTimer() {
        this.lastActivity = Date.now();
        this.isWarningShown = false;
        
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
        }
        
        this.timeoutId = setTimeout(() => {
            this.forceLogout();
        }, this.timeoutDuration);
    }
    
    forceLogout() {
        if (this.isWarningShown) {
            return;
        }
        
        this.isWarningShown = true;
        
        // Afficher le message
        alert('Votre session a expiré. Veuillez vous reconnecter.');
        
        // VRAIMENT déconnecter en appelant /logout
        window.location.href = '/logout';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    if (!window.location.pathname.includes('/auth/login') && 
        !window.location.pathname.includes('/register')) {
        new SessionManager(30); // 1 minute pour tester
    }
});
