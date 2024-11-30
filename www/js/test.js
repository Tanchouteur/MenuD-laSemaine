const parser = new DOMParser();
const xmlDoc = parser.parseFromString(xmlText, "application/xml");

menuContainer.innerHTML = "";

const jours = xmlDoc.getElementsByTagName("jour");
const today = new Date();
const options = { weekday: 'long' }; // Pour obtenir le nom du jour en texte complet
const currentDayName = today.toLocaleDateString('fr-FR', options).toUpperCase(); // Mettre le jour en majuscules

for (const jour of jours) {
    const jourNumero = jour.getAttribute("numero"); // Récupérer le numéro du jour
    const jourName = ["lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"][jourNumero - 1].toUpperCase(); // Convertir le numéro en nom de jour

    // Créer un objet Date pour le jour
    const jourDate = new Date();
    jourDate.setDate(today.getDate() + (parseInt(jourNumero) - today.getDay() + 7) % 7); // Calculer la date à partir du jour de la semaine

    const card = document.createElement("div");
    card.classList.add("card");
    card.id = jourName;

    const jourTitle = document.createElement("h2");

    // Vérifier si le jour actuel est le même que le jour du menu
    if (jourName === currentDayName) {
        card.classList.add("current-day");
        jourTitle.classList.add("current-day");
    }

    jourTitle.textContent = jourName;
    card.appendChild(jourTitle);

    // Récupérer les repas du jour
    const repasElements = jour.getElementsByTagName("repas");

    for (const repas of repasElements) {
        const entreeElement = repas.getElementsByTagName("entree")[0];
        const platComposeElement = repas.getElementsByTagName("platCompose")[0];

        const repasDiv = document.createElement("div");
        repasDiv.classList.add("meal-description");

        // Ajouter des détails sur l'entrée, s'il y en a une
        const entree = entreeElement ? entreeElement.textContent : "Aucune";

        // Ajouter des détails sur le plat composé
        let platCompose = "Aucun";
        if (platComposeElement) {
            const viande = platComposeElement.getElementsByTagName("viande")[0].textContent;
            const accompagnement = platComposeElement.getElementsByTagName("accompagnement")[0].textContent;
            platCompose = `${viande}, ${accompagnement}`;
        }

        repasDiv.innerHTML = `
            <p><b>Entrée : </b>${entree}</p>
            <p><b>Plat : </b>${platCompose}</p>
        `;

        card.appendChild(repasDiv);
    }

    if (jourDate < today) {
        card.classList.add("past-day"); // Appliquer la classe pour les jours passés
    }

    menuContainer.appendChild(card);
}
