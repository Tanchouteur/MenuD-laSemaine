package fr.tanchou.menudlasemaine.models;

public class Viande {
    private int viandeId;
    private String nom;

    public Viande(int viandeId, String nom) {
        this.viandeId = viandeId;
        this.nom = nom;
    }

    public int getViandeId() {
        return viandeId;
    }

    public void setViandeId(int viandeId) {
        this.viandeId = viandeId;
    }

    public String getNomViande() {
        return nom;
    }

    public void setNomViande(String nom) {
        this.nom = nom;
    }
}
