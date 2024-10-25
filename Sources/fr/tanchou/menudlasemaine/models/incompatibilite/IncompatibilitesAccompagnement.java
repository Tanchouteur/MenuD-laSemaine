package fr.tanchou.menudlasemaine.models.incompatibilite;

public class IncompatibilitesAccompagnement {
    private final String legumeNom;
    private final String feculentNom;

    public IncompatibilitesAccompagnement(String legumeNom, String feculentNom) {
        this.legumeNom = legumeNom;
        this.feculentNom = feculentNom;
    }

    @Override
    public String toString() {
        return legumeNom + " et " + feculentNom + " ne vont pas ensemble.";
    }
}
