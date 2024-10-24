package fr.tanchou.menudlasemaine.models;

public class Viande {
    private String nom;

    public Viande(String nom) {
        this.nom = nom;
    }

    public String getNomViande() {
        return nom;
    }

    public void setNomViande(String nom) {
        this.nom = nom;
    }
}
