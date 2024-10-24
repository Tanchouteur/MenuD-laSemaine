package fr.tanchou.menudlasemaine.models;

public class PoidsFamille {
    private int poidsFamilleId;
    private Plat plat;
    private int membre;
    private int poids;

    public PoidsFamille(int poidsFamilleId, Plat plat, int membre, int poids) {
        this.poidsFamilleId = poidsFamilleId;
        this.plat = plat;
        this.membre = membre;
        this.poids = poids;
    }

    public int getPoidsFamilleId() {
        return poidsFamilleId;
    }

    public void setPoidsFamilleId(int poidsFamilleId) {
        this.poidsFamilleId = poidsFamilleId;
    }

    public Plat getPlat() {
        return plat;
    }

    public void setPlat(Plat plat) {
        this.plat = plat;
    }

    public int getMembre() {
        return membre;
    }

    public void setMembre(int membre) {
        this.membre = membre;
    }

    public int getPoids() {
        return poids;
    }

    public void setPoids(int poids) {
        this.poids = poids;
    }
}
