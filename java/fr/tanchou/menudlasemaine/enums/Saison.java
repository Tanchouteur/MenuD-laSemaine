package fr.tanchou.menudlasemaine.enums;

public enum Saison {
    HIVER("Hiver"),
    PRINTEMPS("Printemps"),
    ETE("Ete"),
    AUTOMNE("Automne");

    private final String description;

    // Constructeur pour assigner une description à chaque saison
    Saison(String description) {
        this.description = description;
    }

    // Méthode pour obtenir la description
    public String getDescription() {
        return description;
    }

    // Méthode pour vérifier si une saison est active pour un mois donné
    public static Saison getSaisonByMois(int mois) {
        if (mois >= 12 || mois <= 2) {
            return HIVER;
        } else if (mois >= 3 && mois <= 5) {
            return PRINTEMPS;
        } else if (mois >= 6 && mois <= 8) {
            return ETE;
        } else {
            return AUTOMNE;
        }
    }

    // Méthode pour vérifier si une saison est active pour un mois donné
    public static int getSaisonIndexByMois(int mois) {
        if (mois >= 12 || mois <= 2) {
            return 3;
        } else if (mois >= 3 && mois <= 5) {
            return 0;
        } else if (mois >= 6 && mois <= 8) {
            return 1;
        } else {
            return 2;
        }
    }
}
