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