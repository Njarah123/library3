document.addEventListener('DOMContentLoaded', function() {
    // Animation des champs
    const formGroups = document.querySelectorAll('.form-group');
    
    formGroups.forEach((group, index) => {
        group.style.opacity = '0';
        group.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            group.style.transition = 'all 0.5s ease';
            group.style.opacity = '1';
            group.style.transform = 'translateY(0)';
        }, index * 50);
    });

    // Animation des labels et validation
    const inputs = document.querySelectorAll('input, select, textarea');
    
    inputs.forEach(input => {
        const label = input.previousElementSibling;
        
        // Animation au focus
        input.addEventListener('focus', () => {
            label.style.color = 'var(--primary-color)';
            label.style.transform = 'translateY(-3px)';
        });

        // Retour à la normale au blur
        input.addEventListener('blur', () => {
            label.style.color = 'var(--text-color)';
            label.style.transform = 'translateY(0)';
            
            // Validation simple
            if (input.hasAttribute('required')) {
                if (!input.value.trim()) {
                    input.style.borderColor = 'var(--danger-color)';
                } else {
                    input.style.borderColor = 'var(--success-color)';
                }
            }
        });
    });

    // Animation du bouton de sauvegarde
    const saveButton = document.querySelector('.btn-save');
    
    if (saveButton) {
        saveButton.addEventListener('mousemove', (e) => {
            const rect = saveButton.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            
            saveButton.style.setProperty('--x', `${x}px`);
            saveButton.style.setProperty('--y', `${y}px`);
        });
    }

    // Confirmation avant annulation
    const cancelButton = document.querySelector('.btn-cancel');
    
    if (cancelButton) {
        cancelButton.addEventListener('click', (e) => {
            const hasChanges = Array.from(inputs).some(input => input.value !== input.defaultValue);
            
            if (hasChanges) {
                if (!confirm('Voulez-vous vraiment annuler les modifications ?')) {
                    e.preventDefault();
                }
            }
        });
    }
});