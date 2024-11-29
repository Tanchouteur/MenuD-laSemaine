function toggleDropdown() {
    const menu = document.querySelector('.dropdown-menu');
    menu.classList.toggle('show');
}

function goToHome() {
    window.location.href = 'https://menu.tanchou.fr'; // Redirige vers l'accueil
}

function goToCategory(category) {
    window.location.href = `/produits/${category}.html`; // Redirige vers la sous-catégorie
}
