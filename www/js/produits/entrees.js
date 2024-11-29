import { addProduct, deleteProduct, fetchProducts } from './products.js';

document.addEventListener('DOMContentLoaded', () => {
    fetchProducts('entree').then(renderEntries);

    document.getElementById('addEntryButton').addEventListener('click', () => {
        const entryName = document.getElementById('entryName').value;
        addProduct('entree', entryName).then(() => {
            fetchProducts('entree').then(renderEntries);
        });
    });
});
