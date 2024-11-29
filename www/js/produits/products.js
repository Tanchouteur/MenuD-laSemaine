function fetchProducts(type) {
    return fetch(`https://tanchou.fr:8090/products/${type}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur HTTP : ${response.status}`);
            }
            return response.json();
        })
        .then(data => data) // Retourne les produits
        .catch(error => {
            console.error('Erreur lors de la récupération des produits :', error);
            return []; // Retourne un tableau vide en cas d'erreur
        });
}

function renderProducts(products) {
    const entriesList = document.getElementById('productsList');
    entriesList.innerHTML = ''; // Vide la liste avant de la remplir

    if (products.length === 0) {
        entriesList.innerHTML = '<li>Aucun produit trouvé.</li>';
        return;
    }

    products.forEach(product => {
        const listItem = document.createElement('li');
        listItem.className = 'list-products';

        // Structure pour mobile
        listItem.innerHTML = `
        <span class="product-name">${product.nom}</span>
        <span class="product-details">Poids : ${product.poids}<br>Last use : ${product.last_use}</span>
    `;

        entriesList.appendChild(listItem);
    });

}

function addProduct(type, name) {
    return fetch(`https://tanchou.fr:8090/products/add/${type}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ nom: name }),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur HTTP : ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('Produit ajouté avec succès :', data);
            return data;
        })
        .catch(error => {
            console.error('Erreur lors de l\'ajout du produit :', error);
        });
}

document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const type = urlParams.get('categorie'); // Ex. "entree"

    if (type) {
        // Met à jour les titres de la page
        document.getElementById('title').textContent = `Produits de type : ${type.charAt(0).toUpperCase() + type.slice(1)}`;
        document.getElementById('addEntryTitle').textContent = `Ajouter une ${type}`;

        // Récupère et affiche les produits
        fetchProducts(type).then(products => {
            renderProducts(products);
        });

        // Ajoute un produit quand le bouton est cliqué
        document.getElementById('addEntryButton').addEventListener('click', () => {
            const entryName = document.getElementById('entryName').value;
            if (entryName) {
                addProduct(type, entryName).then(() => {
                    fetchProducts(type).then(products => {
                        renderProducts(products);
                    });
                });
            } else {
                alert('Veuillez entrer un nom pour l\'entrée.');
            }
        });
    } else {
        console.error('Le paramètre "categorie" est manquant dans l\'URL.');
        document.getElementById('title').textContent = 'Erreur : Paramètre de catégorie manquant';
    }
});