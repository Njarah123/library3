document.addEventListener('DOMContentLoaded', function() {
    // Animation des champs de formulaire
    const formGroups = document.querySelectorAll('.form-group');
    
    formGroups.forEach((group, index) => {
        group.style.opacity = '0';
        group.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            group.style.transition = 'all 0.5s ease';
            group.style.opacity = '1';
            group.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // Validation en temps réel
    const inputs = document.querySelectorAll('.form-control');
    
    inputs.forEach(input => {
        input.addEventListener('input', function() {
            validateInput(this);
        });
    });

    // Animation des labels
    const labels = document.querySelectorAll('label');
    
    labels.forEach(label => {
        const input = document.getElementById(label.getAttribute('for'));
        if (input) {
            input.addEventListener('focus', () => {
                label.style.transform = 'translateY(-3px)';
                label.style.color = 'var(--primary-color)';
            });

            input.addEventListener('blur', () => {
                label.style.transform = 'translateY(0)';
                label.style.color = 'var(--text-color)';
            });
        }
    });

    // Fonction de validation
    function validateInput(input) {
        const isValid = input.checkValidity();
        if (isValid) {
            input.style.borderColor = 'var(--success-color)';
        } else {
            input.style.borderColor = 'var(--danger-color)';
        }
    }

    // Animation du bouton submit
    const submitBtn = document.querySelector('.btn-primary');
    
    submitBtn.addEventListener('mouseover', function(e) {
        const x = e.pageX - this.offsetLeft;
        const y = e.pageY - this.offsetTop;

        this.style.setProperty('--x', x + 'px');
        this.style.setProperty('--y', y + 'px');
    });
});