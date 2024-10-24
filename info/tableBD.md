# SQL pour créer les tables de la base de données
```sql
-- Table Menu
CREATE TABLE Menu (
    menu_id INT AUTO_INCREMENT PRIMARY KEY,
    lundi_midi_repas_id INT,
    lundi_soir_repas_id INT,
    mardi_midi_repas_id INT,
    mardi_soir_repas_id INT,
    -- Ajoute les autres jours de la semaine
    FOREIGN KEY (lundi_midi_repas_id) REFERENCES Repas(repas_id),
    FOREIGN KEY (lundi_soir_repas_id) REFERENCES Repas(repas_id),
    FOREIGN KEY (mardi_midi_repas_id) REFERENCES Repas(repas_id),
    FOREIGN KEY (mardi_soir_repas_id) REFERENCES Repas(repas_id)
);

-- Table Repas
CREATE TABLE Repas (
    repas_id INT AUTO_INCREMENT PRIMARY KEY,
    entree_id INT,
    plat_id INT,
    FOREIGN KEY (entree_id) REFERENCES Entree(entree_id),
    FOREIGN KEY (plat_id) REFERENCES Plat(plat_id)
);

-- Table Entree
CREATE TABLE Entree (
    entree_id INT AUTO_INCREMENT PRIMARY KEY,
    nom_entree VARCHAR(255) NOT NULL
);

-- Table Plat
CREATE TABLE Plat (
    plat_id INT AUTO_INCREMENT PRIMARY KEY,
    viande_id INT,
    accompagnement_id INT,
    tout_en_un BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (viande_id) REFERENCES Viande(viande_id),
    FOREIGN KEY (accompagnement_id) REFERENCES Accompagnement(accompagnement_id)
);

-- Table Viande
CREATE TABLE Viande (
    viande_id INT AUTO_INCREMENT PRIMARY KEY,
    nom_viande VARCHAR(255) NOT NULL
);

-- Table Accompagnement
CREATE TABLE Accompagnement (
    accompagnement_id INT AUTO_INCREMENT PRIMARY KEY,
    legume_id INT,
    feculent_id INT,
    FOREIGN KEY (legume_id) REFERENCES Legume(legume_id),
    FOREIGN KEY (feculent_id) REFERENCES Feculent(feculent_id)
);

-- Table Legume
CREATE TABLE Legume (
    legume_id INT AUTO_INCREMENT PRIMARY KEY,
    nom_legume VARCHAR(255) NOT NULL
);

-- Table Feculent
CREATE TABLE Feculent (
    feculent_id INT AUTO_INCREMENT PRIMARY KEY,
    nom_feculent VARCHAR(255) NOT NULL
);

-- Table PoidsFamille
CREATE TABLE PoidsFamille (
    poids_id INT AUTO_INCREMENT PRIMARY KEY,
    plat_id INT,
    membre_famille_id INT NOT NULL, -- 1-5 pour les 5 membres
    poids FLOAT NOT NULL,
    FOREIGN KEY (plat_id) REFERENCES Plat(plat_id)
);

-- Table PoidsSaison
CREATE TABLE PoidsSaison (
    poids_id INT AUTO_INCREMENT PRIMARY KEY,
    plat_id INT,
    saison ENUM('hiver', 'printemps', 'été', 'automne') NOT NULL,
    poids FLOAT NOT NULL,
    FOREIGN KEY (plat_id) REFERENCES Plat(plat_id)
);

-- Table PoidsMomentJournee
CREATE TABLE PoidsMomentJournee (
    poids_id INT AUTO_INCREMENT PRIMARY KEY,
    plat_id INT,
    moment ENUM('midi', 'soir') NOT NULL,
    poids FLOAT NOT NULL,
    FOREIGN KEY (plat_id) REFERENCES Plat(plat_id)
);

-- Table PoidsMomentSemaine
CREATE TABLE PoidsMomentSemaine (
    poids_id INT AUTO_INCREMENT PRIMARY KEY,
    plat_id INT,
    semaine_weekend ENUM('semaine', 'weekend') NOT NULL,
    poids FLOAT NOT NULL,
    FOREIGN KEY (plat_id) REFERENCES Plat(plat_id)
);

-- Table Incompatibiliter
CREATE TABLE Incompatibilite (
     incompatibilite_id INT AUTO_INCREMENT PRIMARY KEY,
     viande_id INT, -- Peut être NULL si l'incompatibilité concerne uniquement des accompagnements
     legume_id INT, -- Peut être NULL si l'incompatibilité concerne uniquement la viande
     feculent_id INT, -- Peut être NULL si l'incompatibilité ne concerne pas de féculent
     FOREIGN KEY (viande_id) REFERENCES Viande(viande_id),
     FOREIGN KEY (legume_id) REFERENCES Legume(legume_id),
     FOREIGN KEY (feculent_id) REFERENCES Feculent(feculent_id)
);
    
```

## SQL exemple d'insertion de données
```sql
INSERT INTO Incompatibilite (viande_id, feculent_id) 
VALUES ((SELECT viande_id FROM Viande WHERE nom_viande = 'poisson'), 
        (SELECT feculent_id FROM Feculent WHERE nom_feculent = 'frites'));

INSERT INTO Incompatibilite (legume_id, feculent_id)
VALUES ((SELECT legume_id FROM Legume WHERE nom_legume = 'brocoli'),
        (SELECT feculent_id FROM Feculent WHERE nom_feculent = 'riz'));

INSERT INTO Incompatibilite (viande_id, legume_id)
VALUES ((SELECT viande_id FROM Viande WHERE nom_viande = 'poulet'),
        (SELECT legume_id FROM Legume WHERE nom_legume = 'tomate'));

```