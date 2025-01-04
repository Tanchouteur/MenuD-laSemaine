document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    const type = urlParams.get('categorie'); // Ex. "entree"

    if (type) {
        // Mise à jour des titres de la page
        document.getElementById('addEntryTitle').textContent = `Ajouter une ${type}`;

        // Chargement initial des produits
        fetchProducts(type).then(products => {
            productList = products; // On met à jour l'objet global
            renderProducts();
        });

    } else {
        console.error('Le paramètre "categorie" est manquant dans l\'URL.');
        document.getElementById('title').textContent = 'Erreur : Paramètre de catégorie manquant';
    }
});

// Objet global pour stocker les produits
let productList = [];

// Récupérer les produits depuis l'API
function fetchProducts(type) {
    return fetch(`https://tanchou.fr:8090/products/get/${type}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Erreur HTTP : ${response.status}`);
            }
            return response.json();
        })
        .catch(error => {
            console.error('Erreur lors de la récupération des produits :', error);
            return [];
        });
}

// Rendre tous les produits depuis l'objet global
function renderProducts() {
    const entriesList = document.getElementById('productsList');
    entriesList.innerHTML = ''; // On vide la liste avant de la remplir

    if (productList.length === 0) {
        entriesList.innerHTML = '<li>Aucun produit trouvé.</li>';
        return;
    }

    productList.forEach(product => {
        const listItem = document.createElement('li');
        listItem.className = 'list-products';

        listItem.innerHTML = `
            <h3 class="product-name">${product.nom}</h3>
            <div class="product-details">
            
                <div class="product-details-group">
                    <h4 class="value-title">Poids Arbitraire :</h4> 
                    <span class="product-value" id="${product.nom}poids_arbitraire">${product.poids_arbitraire}</span>
                </div>
                
                <div class="product-details-section">
                    <h4 class="value-title">Poids moment</h4>
                    
                    <div class="product-details-group">
                        <span class="value-info">Midi semaine :</span><span class="product-value" id="${product.nom}poids_midiSemaine">${product.poids_midiSemaine}</span>
                    </div>
                    <div class="product-details-group">
                        <span class="value-info">Soir semaine :</span><span class="product-value" id="${product.nom}poids_soirSemaine">${product.poids_soirSemaine}</span>
                    </div>
                    <div class="product-details-group">
                        <span class="value-info">Midi weekend :</span><span class="product-value" id="${product.nom}poids_midiWeekend">${product.poids_midiWeekend}</span>
                    </div>
                    <div class="product-details-group">
                        <span class="value-info">Soir weekend :</span><span class="product-value" id="${product.nom}poids_soirWeekend">${product.poids_soirWeekend}</span>
                    </div>
                </div>
                
                <div class="product-details-section">
                    <h4 class="value-title">Poids saison</h4>
                    
                    <div class="product-details-group">
                        <span class="value-info">Printemps :</span><span class="product-value" id="${product.nom}poids_printemps">${product.poids_printemps}</span>
                    </div>
                    <div class="product-details-group">
                        <span class="value-info">Eté :</span><span class="product-value" id="${product.nom}poids_ete">${product.poids_ete}</span>
                    </div>
                    <div class="product-details-group">
                        <span class="value-info">Automne :</span><span class="product-value" id="${product.nom}poids_automne">${product.poids_automne}</span>
                    </div>
                    <div class="product-details-group">
                        <span class="value-info">Hiver :</span><span class="product-value" id="${product.nom}poids_hiver">${product.poids_hiver}</span>
                    </div>
                </div>
                <div class="product-details-group">
                    <h4 class="value-title">Dernière utilisation :</h4><span class="product-value" id="${product.nom}last_use">${product.last_use}</span>
                </div>
            </div>
            <div class="product-buttons-group">
                <button class="product-button modifier" onclick="editProduct('${product.nom}')">Modifier</button>
                <button class="product-button supprimer" onclick="deleteProduct('${product.nom}')">Supprimer</button>
            </div>
        `;

        entriesList.appendChild(listItem);
    });
}

// Ajouter un produit
document.getElementById("addEntryButton").addEventListener("click", function () {
    const urlParams = new URLSearchParams(window.location.search);
    const type = urlParams.get('categorie');
    const entryForm = document.getElementById("entryForm");

    if (entryForm.querySelector("#entryName").value === "") {
        alert("Veuillez entrer un nom pour le produit.");
        return;
    }

    // Création de l'objet produit
    const newProduct = {
        nomProduit: entryForm.querySelector("#entryName").value,
        poidsArbitraire: parseInt(entryForm.querySelector("#poidsArbitraire").value, 10),
        typeProduit: type.toUpperCase(),
        poidsMoment: [
            parseInt(entryForm.querySelector("#midiSemaine").value, 10),
            parseInt(entryForm.querySelector("#soirSemaine").value, 10),
            parseInt(entryForm.querySelector("#midiWeekend").value, 10),
            parseInt(entryForm.querySelector("#soirWeekend").value, 10),
        ],
        poidsSaison: [
            parseInt(entryForm.querySelector("#printemps").value, 10),
            parseInt(entryForm.querySelector("#ete").value, 10),
            parseInt(entryForm.querySelector("#automne").value, 10),
            parseInt(entryForm.querySelector("#hiver").value, 10),
        ]
    };

    addProduct(type, newProduct);
});

function addProduct(type, newProduct) {
    fetch(`https://tanchou.fr:8090/products/add/${type}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(newProduct)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error("Erreur lors de l'ajout du produit : " + response.statusText);
            }
            return response.json();
        })
        .then(addedProduct => {
            console.log("Produit ajouté avec succès :", addedProduct);

            // Mise à jour de la liste globale
            productList.push(addedProduct);

            // Rafraîchissement de l'affichage
            renderProducts();
        })
        .catch(error => {
            console.error("Erreur :", error);
            alert("Erreur lors de l'ajout du produit.");
        });
}

//Supprimer un produit
function deleteProduct(productName) {
    if (confirm(`Voulez-vous vraiment supprimer le produit "${productName}" ?`)) {
        fetch(`https://tanchou.fr:8090/products/delete/`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: productName
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Erreur lors de l'ajout du produit ");
            }
            return response.json();
        })
        .then(deletedProduct => {
            console.log("Produit supprimé avec succès :", deletedProduct);

            // Mise à jour de la liste globale
            productList = productList.filter(product => product.nom !== productName);

            // Rafraîchissement de l'affichage
            renderProducts();
        })
    }
}
