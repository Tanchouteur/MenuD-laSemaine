package fr.tanchou.menudlasemaine.models;

public class Plat {
    private int platId;
    private Viande viande;
    private Accompagnement accompagnement;
    private boolean toutEnUn;
    private String nomPlat;

    public Plat(int platId, Viande viande, Accompagnement accompagnement, boolean toutEnUn, String nomPlat) {
        this.platId = platId;
        this.viande = viande;
        this.accompagnement = accompagnement;
        this.toutEnUn = toutEnUn;
        this.nomPlat = nomPlat;
    }

    // Getters
    public int getPlatId() {
        return platId;
    }

    public Viande getViande() {
        return viande;
    }

    public Accompagnement getAccompagnement() {
        return accompagnement;
    }

    public boolean isToutEnUn() {
        return toutEnUn;
    }

    public String getNomPlat() {
        return nomPlat;
    }

    // Setters
    public void setPlatId(int platId) {
        this.platId = platId;
    }

    public void setViande(Viande viande) {
        this.viande = viande;
    }

    public void setAccompagnement(Accompagnement accompagnement) {
        this.accompagnement = accompagnement;
    }

    public void setToutEnUn(boolean toutEnUn) {
        this.toutEnUn = toutEnUn;
    }

    public void setNomPlat(String nomPlat) {
        this.nomPlat = nomPlat;
    }
}
