package fr.tanchou.menudlasemaine.models;

public class Entree {
    private String nomEntree;

    public Entree(String nomEntree) {
        this.nomEntree = nomEntree;
    }

    public String getNomEntree() {
        return nomEntree;
    }

    public void setNomEntree(String nomEntree) {
        this.nomEntree = nomEntree;
    }
}
