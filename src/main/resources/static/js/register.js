function toggleFields() {
    const userType = document.getElementById('userType').value;
    const studentFields = document.getElementById('studentFields');
    const staffFields = document.getElementById('staffFields');
    
    // Supprimer la classe active de tous les champs
    studentFields.classList.remove('active');
    staffFields.classList.remove('active');
    
    // Ajouter la classe active aux champs appropriés
    if (userType === 'STUDENT') {
        studentFields.classList.add('active');
    } else if (userType === 'STAFF' || userType === 'LIBRARIAN') {
        staffFields.classList.add('active');
    }
    
    // Mettre à jour les attributs required
    const studentInputs = studentFields.querySelectorAll('input');
    const staffInputs = staffFields.querySelectorAll('input');
    
    studentInputs.forEach(input => {
        input.required = userType === 'STUDENT';
    });
    
    staffInputs.forEach(input => {
        input.required = userType === 'STAFF' || userType === 'LIBRARIAN';
    });
}

document.addEventListener('DOMContentLoaded', toggleFields);