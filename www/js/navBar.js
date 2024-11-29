function toggleDropdown() {
    const menu = document.querySelector('.dropdown-menu');
    menu.classList.toggle('show');
}

function goToHome() {
    window.location.href = 'https://menu.tanchou.fr';
}

function goToCategory(category) {
    window.location.href = `/produits/produits.html?categorie=${category}`;
}
