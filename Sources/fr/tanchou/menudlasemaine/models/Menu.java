package fr.tanchou.menudlasemaine.models;

public class Menu {
    private Repas[][] repasParJour;

    public Menu(Repas[][] repasParJour) {
        this.repasParJour = repasParJour;
    }

    public Repas[][] getRepasParJour() {
        return repasParJour;
    }
}
