document.addEventListener('DOMContentLoaded', function() {
    // Gestion de l'affichage/masquage des filtres avancés
    const toggleFiltersBtn = document.getElementById('toggleFilters');
    const filtersContent = document.getElementById('filtersContent');
    
    if (toggleFiltersBtn && filtersContent) {
        toggleFiltersBtn.addEventListener('click', function() {
            filtersContent.classList.toggle('hidden');
            
            // Changer l'icône et le texte du bouton
            if (filtersContent.classList.contains('hidden')) {
                toggleFiltersBtn.innerHTML = '<i class="fas fa-filter"></i> Afficher les filtres';
            } else {
                toggleFiltersBtn.innerHTML = '<i class="fas fa-filter"></i> Masquer les filtres';
            }
        });
    }
    
    // Gestion du bouton de réinitialisation des filtres
    const resetBtn = document.querySelector('.btn-reset');
    if (resetBtn) {
        resetBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Réinitialiser tous les champs de filtre
            const filterForm = document.getElementById('advancedFiltersForm');
            const selects = filterForm.querySelectorAll('select');
            const inputs = filterForm.querySelectorAll('input[type="number"]');
            
            selects.forEach(select => {
                select.value = '';
            });
            
            inputs.forEach(input => {
                input.value = '';
            });
            
            // Soumettre le formulaire pour appliquer la réinitialisation
            filterForm.submit();
        });
    }
    
    // Gestion de la sidebar des catégories
    const toggleCategoriesSidebarBtn = document.getElementById('toggleCategoriesSidebar');
    const categoriesSidebar = document.getElementById('categoriesSidebar');
    const closeCategoriesSidebarBtn = document.getElementById('closeCategoriesSidebarBtn');
    
    if (toggleCategoriesSidebarBtn && categoriesSidebar) {
        toggleCategoriesSidebarBtn.addEventListener('click', function() {
            categoriesSidebar.classList.toggle('active');
        });
    }
    
    if (closeCategoriesSidebarBtn && categoriesSidebar) {
        closeCategoriesSidebarBtn.addEventListener('click', function() {
            categoriesSidebar.classList.remove('active');
        });
    }
    
    // Fermer la sidebar des catégories en cliquant en dehors
    document.addEventListener('click', function(e) {
        if (categoriesSidebar && categoriesSidebar.classList.contains('active') && 
            !categoriesSidebar.contains(e.target) && 
            e.target !== toggleCategoriesSidebarBtn && 
            !toggleCategoriesSidebarBtn.contains(e.target)) {
            categoriesSidebar.classList.remove('active');
        }
    });
    
    // Gestion des options de tri
    const sortOptions = document.querySelectorAll('.sort-option');
    
    sortOptions.forEach(option => {
        option.addEventListener('click', function(e) {
            const currentUrl = new URL(this.href);
            const sortBy = currentUrl.searchParams.get('sortBy');
            const ascending = currentUrl.searchParams.get('ascending');
            
            // Si l'option est déjà active, inverser l'ordre de tri
            if (this.classList.contains('active')) {
                e.preventDefault();
                
                // Inverser l'ordre de tri
                const newAscending = ascending === 'true' ? 'false' : 'true';
                
                // Mettre à jour l'URL et rediriger
                currentUrl.searchParams.set('ascending', newAscending);
                window.location.href = currentUrl.toString();
            }
        });
    });
    
    // Initialiser l'état des filtres (masqués par défaut)
    if (filtersContent) {
        filtersContent.classList.add('hidden');
        if (toggleFiltersBtn) {
            toggleFiltersBtn.innerHTML = '<i class="fas fa-filter"></i> Afficher les filtres';
        }
    }
});