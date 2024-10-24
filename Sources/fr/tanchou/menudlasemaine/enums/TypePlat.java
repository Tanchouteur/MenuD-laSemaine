package fr.tanchou.menudlasemaine.enums;

public enum TypePlat {
    COMPOSE("compose"),
    COMPLET("complet");

    private final String label;

    TypePlat(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TypePlat fromString(String label) {
        for (TypePlat type : TypePlat.values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Type de plat non valide : " + label);
    }
}
