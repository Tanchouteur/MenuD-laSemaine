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
            const options = { weekday: 'long' }; // Pour obtenir le nom du jour en texte complet
            const currentDayName = today.toLocaleDateString('fr-FR', options).toUpperCase(); // Mettre le jour en majuscules

            for (const jour of jours) {
                const jourName = jour.getAttribute("nom").toUpperCase(); // Convertir en majuscules

                // Convertir le nom du jour en un objet Date
                const jourDate = new Date();
                jourDate.setDate(today.getDate() + (Array.from(jours).indexOf(jour) - today.getDay())); // Calculer la date à partir du jour de la semaine

                const card = document.createElement("div");
                card.classList.add("card");
                card.id = jourName

                const jourTitle = document.createElement("h2");

                // Vérifier si le jour actuel est le même que le jour du menu
                if (jourName === currentDayName) {
                    card.classList.add("current-day");
                    jourTitle.classList.add("current-day");
                }

                jourTitle.textContent = jourName;
                card.appendChild(jourTitle);

                const midi = jour.getElementsByTagName("midi")[0];
                const midiDiv = document.createElement("div");

                midiDiv.classList.add("meal-description");
                midiDiv.classList.add("MIDI");
                midiDiv.innerHTML = `
                <div class="meal-time">Midi : <button class="card-button" id="${jourName}MIDI" onclick="regenerateRepa('${jourName}', 'MIDI')">Regénéré</button></div>
                <p><b>Entrée : </b>${midi.getElementsByTagName("entree")[0]?.textContent || "Aucune"}</p>
                <p><b>Plat : </b>${midi.getElementsByTagName("plat")[0]?.textContent || "Aucun"}</p>
            `;
                card.appendChild(midiDiv);

                const soir = jour.getElementsByTagName("soir")[0];
                const soirDiv = document.createElement("div");
                soirDiv.classList.add("meal-description");
                soirDiv.classList.add("SOIR");
                soirDiv.innerHTML = `
                <div class="meal-time">Soir : <button class="card-button" id="${jourName}SOIR" onclick="regenerateRepa('${jourName}', 'SOIR')">Regénéré</button></div>
                <p><b>Entrée : </b>${soir.getElementsByTagName("entree")[0]?.textContent || "Aucune"}</p>
                <p><b>Plat : </b>${soir.getElementsByTagName("plat")[0]?.textContent || "Aucun"}</p>
            `;
                if (jourDate < today) {
                    card.classList.add("past-day"); // Appliquer la classe pour les jours passés
                    midi.classList.add("past-day");
                    soir.classList.add("past-day");
                }
                card.appendChild(soirDiv);

                menuContainer.appendChild(card);
            }

        })
        .catch(error => {
            menuContainer.innerHTML = "Erreur lors de la récupération du menu.";
            console.error("Erreur de traitement XML:", error);
        });
});

function regenerateRepa(jourName, moment) {

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
                        <div class="meal-time">${moment === "MIDI" ? "Midi" : "Soir"} : <button class="card-button" id="${jourName}SOIR" onclick="regenerateRepa('${jourName}', '${moment}')">Regénéré</button></div>
                        <p><b>Entrée : </b>${entree}</p>
                        <p><b>Plat : </b>${plat}</p>
                    `;
        }
    }

}