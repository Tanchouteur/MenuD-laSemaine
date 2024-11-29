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

                const regenerateButton = document.createElement("button")
                regenerateButton.classList.add("nav-button")
                regenerateButton.innerHTML = ``;

                midiDiv.classList.add("meal-description");
                midiDiv.innerHTML = `
                <div class="meal-time">Midi : <button class="card-button" onclick="regenerateRepa('${jourName}','midi')">Regénéré</button></div>
                <p><b>Entrée : </b>${midi.getElementsByTagName("entree")[0]?.textContent || "Aucune"}</p>
                <p><b>Plat : </b>${midi.getElementsByTagName("plat")[0]?.textContent || "Aucun"}</p>
            `;
                card.appendChild(midiDiv);

                const soir = jour.getElementsByTagName("soir")[0];
                const soirDiv = document.createElement("div");
                soirDiv.classList.add("meal-description");
                soirDiv.innerHTML = `
                <div class="meal-time">Soir : <button class="card-button" onclick="regenerateRepa('${jourName}','soir')">Regénéré</button></div>
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
    const errContainer = document.getElementById("errContainer");
    console.log("fetch : " + "https://tanchou.fr:8090/menu/repas/change?jour=" + jourName.toString() + "&moment=" + moment.toString());
    fetch("https://tanchou.fr:8090/menu/repas/change?jour=" + jourName.toString() + "&moment=" + moment.toString())
        .then(r  => {
            refresh();
        })
        .catch(error => {
            errContainer.innerHTML = "<p>Erreur lors du rechargement du repas.</p>";
        })
}

function refresh(){
    window.location.href = 'https://menu.tanchou.fr';
}