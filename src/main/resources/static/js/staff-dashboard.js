document.addEventListener('DOMContentLoaded', function() {
    // Mise à jour de l'heure en temps réel
    function updateTime() {
        const timeElement = document.querySelector('.current-time');
        const now = new Date();
        const formattedTime = now.toLocaleString('fr-FR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
        timeElement.textContent = formattedTime;
    }

    setInterval(updateTime, 1000);

    // Animation des cartes au survol
    const cards = document.querySelectorAll('.dashboard-card');

    cards.forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const centerX = rect.width / 2;
            const centerY = rect.height / 2;

            const rotateX = (y - centerY) / 20;
            const rotateY = (centerX - x) / 20;

            card.style.transform = `
                translateY(-10px)
                perspective(1000px)
                rotateX(${rotateX}deg)
                rotateY(${rotateY}deg)
            `;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = '';
        });
    });

    // Animation du texte du header
    const title = document.querySelector('h1');
    const text = title.textContent;
    title.textContent = '';

    for (let i = 0; i < text.length; i++) {
        const span = document.createElement('span');
        span.textContent = text[i];
        span.style.animationDelay = `${i * 0.1}s`;
        title.appendChild(span);
    }
});