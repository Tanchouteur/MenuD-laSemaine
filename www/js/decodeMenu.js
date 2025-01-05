document.addEventListener("DOMContentLoaded", () => {
    const menuContainer = document.getElementById("menuContainer");

    fetch("https://tanchou.fr:8090/menu/getMenu")
        .then(response => response.text())
        .then(xmlText => {
            const parser = new DOMParser();
            const xmlDoc = parser.parseFromString(xmlText, "application/xml");

            menuContainer.innerHTML = "";

            const jours = xmlDoc.getElementsByTagName("jour");
            const today = new Date();
            const options = { weekday: 'long' };
            const currentDayName = today.toLocaleDateString('fr-FR', options).toUpperCase();

            for (const jour of jours) {
                const jourNumero = jour.getAttribute("numero");
                const jourName = ["lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche"][jourNumero - 1].toUpperCase(); // Convertir le numéro en nom de jour

                // Créer la carte pour chaque jour
                const card = document.createElement("div");
                card.classList.add("card");
                card.id = `${jourName}`;

                const jourTitle = document.createElement("h2");
                jourTitle.textContent = `${jourName}`;
                card.appendChild(jourTitle);

                if (jourNumero == today.getDay() + 1) {
                    card.classList.add("current-day");
                    jourTitle.classList.add("current-day");
                }

                // Créer des sections pour le midi et le soir
                const midiDiv = document.createElement("div");
                midiDiv.classList.add("meal-description");
                midiDiv.classList.add("MIDI");
                midiDiv.innerHTML = `<div class="meal-time">Midi : <button class="card-button" id="${jourName}MIDI" onclick="regenerateRepas('${jourName}','MIDI')">Regénérer</button></div>`;

                const soirDiv = document.createElement("div");
                soirDiv.classList.add("meal-description");
                soirDiv.classList.add("SOIR");
                soirDiv.innerHTML = `<div class="meal-time">Soir : <button class="card-button" id="${jourName}SOIR" onclick="regenerateRepas('${jourName}','SOIR')">Regénérer</button></div>`;

                // Initialiser le contenu des repas avec "Aucune" par défaut
                let midiRepasContent = "<p><b>Entrée : </b>Aucune</p><p><b>Plat : </b>Aucune</p>";
                let soirRepasContent = "<p><b>Entrée : </b>Aucune</p><p><b>Plat : </b>Aucune</p>";

                // Parcourir chaque élément de repas pour le jour
                const repasElements = jour.getElementsByTagName("repas");
                for (let i = 0; i < repasElements.length; i++) {
                    const repas = repasElements[i];
                    const entree = repas.getElementsByTagName("entree")[0];
                    const platCompose = repas.getElementsByTagName("platCompose")[0];

                    if (entree) {
                        // Modifier l'entrée si elle existe
                        if (i === 0) {
                            midiRepasContent = `<p><b>Entrée : </b>${entree.textContent}</p><p><b>Plat : </b>Aucune</p>`;
                        } else {
                            soirRepasContent = `<p><b>Entrée : </b>${entree.textContent}</p><p><b>Plat : </b>Aucune</p>`;
                        }
                    }

                    if (platCompose) {
                        const viande = platCompose.getElementsByTagName("viande")[0]?.textContent || "Non spécifié";
                        const accompagnement = platCompose.getElementsByTagName("accompagnement")[0]?.textContent || "Non spécifié";

                        // Modifier le plat dans la section appropriée
                        if (i === 0) {
                            midiRepasContent = `<p><b>Entrée : </b>Aucune</p><p><b>Plat : </b> ${viande} - ${accompagnement}</p>`;
                        } else {
                            soirRepasContent = `<p><b>Entrée : </b>Aucune</p><p><b>Plat : </b> ${viande} - ${accompagnement}</p>`;
                        }
                    }
                }

                // Remplir les sections avec le contenu ou le texte par défaut
                midiDiv.innerHTML += midiRepasContent;
                soirDiv.innerHTML += soirRepasContent;

                card.appendChild(midiDiv);
                card.appendChild(soirDiv);

                menuContainer.appendChild(card);
            }
        })
        .catch(error => {
            menuContainer.innerHTML = "Erreur lors de la récupération du menu.";
            console.error("Erreur de traitement XML:", error);
        });
});

function regenerateRepas(jourName, moment) {

    const buttonElement = document.getElementById(jourName + moment);
    //console.log("bouton : " + buttonElement);

    // Désactiver le bouton
    buttonElement.disabled = true;
    const errContainer = document.getElementById("errContainer");
    const apiURL = "https://tanchou.fr:8090/menu/repas/change?jour=";

    //console.log("fetch : " + apiURL + jourName.toString() + "&moment=" + moment.toString());

    fetch(apiURL + jourName.toString() + "&moment=" + moment.toString())
        .then(response => {
            if (!response.ok) {
                throw new Error("Erreur de réponse: " + response.status);
            }
            return response.text();
        })
        .then(xmlText => {
            try {
                const parser = new DOMParser();

                const xmlDoc = parser.parseFromString(xmlText, "application/xml");

                updateRepas(xmlDoc, jourName, moment);


            } catch (error) {
                console.error("Erreur lors de l'analyse de la réponse XML :", error);
                errContainer.innerHTML = "<p>Erreur lors de l'analyse de la réponse XML.</p>";
            }

        })
        .catch(error => {
            console.error(error);
            errContainer.innerHTML = "<p>Erreur lors du rechargement du repas.</p>";
        });
    // Réactiver le bouton après traitement
    buttonElement.disabled = false;
}

function updateRepas(xmlDoc, jourName, moment) {

// Récupérer le jour et le moment (midi ou soir) du XML
    const jour = xmlDoc.querySelector(`jour[nom="${jourName}"]`);
    if (!jour) {
        throw new Error("Jour non trouvé dans la réponse.");
    }
    moment = moment.toUpperCase();
    const momentElement = jour.querySelector(moment);
    if (!momentElement) {
        throw new Error("Moment non trouvé dans la réponse. Jour : " + jourName + ", Moment : " + moment);
    }

    //console.log("jour : " + jourName + ", moment : " + moment);

    // Extraire l'entrée et le plat
    const entree = momentElement.querySelector("entree")?.textContent || "Aucune";
    const plat = momentElement.querySelector("plat")?.textContent || "Aucun";

    // Trouver la div correspondant au jour et au moment
    const card = document.getElementById(jourName);
    if (card) {
        const momentDiv = moment === "MIDI" ? card.querySelector(".MIDI") :
            card.querySelector(".SOIR");

        if (momentDiv) {
            // Mettre à jour le contenu du moment
            momentDiv.innerHTML = `
                        <div class="meal-time">${moment === "MIDI" ? "Midi" : "Soir"} : <button class="card-button" id="${jourName}${moment}" onclick="regenerateRepas('${jourName}', '${moment}')">Regénéré</button></div>
                        <p><b>Entrée : </b>${entree}</p>
                        <p><b>Plat : </b>${plat}</p>
                    `;
        }
    }

}