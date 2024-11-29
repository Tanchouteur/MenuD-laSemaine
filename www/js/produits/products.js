
function addProduct(type, name) {
    // Logique générique pour ajouter un produit de type `type`
}

function deleteProduct(type, id) {
    // Logique générique pour supprimer un produit de type `type`
}

function fetchProducts(type) {
    fetch(`http://127.0.0.1:8090/products/${type}`)
        .then(response => response.json())
        .then(data => {
            renderProducts(data); // Affiche les produits récupérés
        })
        .catch(error => console.error('Erreur:', error));
}
