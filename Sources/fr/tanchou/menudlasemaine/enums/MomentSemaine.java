package fr.tanchou.menudlasemaine.enums;

public enum MomentSemaine {
    SEMAINE("semaine"),
    WEEKEND("weekend");

    private final String description;

    // Constructeur pour assigner une description à chaque moment
    MomentSemaine(String description) {
        this.description = description;
    }

    // Méthode pour obtenir la description
    public String getDescription() {
        return description;
    }
}
