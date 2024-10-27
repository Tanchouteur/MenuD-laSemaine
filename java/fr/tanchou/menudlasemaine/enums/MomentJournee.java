package fr.tanchou.menudlasemaine.enums;

public enum MomentJournee {
    MIDI("Midi"),
    SOIR("Soir");

    private final String description;

    // Constructeur pour assigner une description à chaque moment de la journée
    MomentJournee(String description) {
        this.description = description;
    }

    // Méthode pour obtenir la description
    public String getDescription() {
        return description;
    }
}
