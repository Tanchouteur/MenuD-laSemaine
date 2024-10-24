package fr.tanchou.menudlasemaine.models;

public class Entree {
    private int entreeId;
    private String nomEntree;

    public Entree(int entreeId, String nomEntree) {
        this.entreeId = entreeId;
        this.nomEntree = nomEntree;
    }

    public int getEntreeId() {
        return entreeId;
    }

    public void setEntreeId(int entreeId) {
        this.entreeId = entreeId;
    }

    public String getNomEntree() {
        return nomEntree;
    }

    public void setNomEntree(String nomEntree) {
        this.nomEntree = nomEntree;
    }
}
