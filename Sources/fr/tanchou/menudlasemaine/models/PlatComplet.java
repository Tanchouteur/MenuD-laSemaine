package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;

public class PlatComplet extends Plat {
    private String nomPlat;

    public PlatComplet(int platId, TypePlat typePlat, float poids, String nomPlat) {
        super(platId, typePlat, poids);
        this.nomPlat = nomPlat;
    }

    public void setNomPlat(String nomPlat) {
        this.nomPlat = nomPlat;
    }

    @Override
    public String toString() {
        return "PlatComplet{" +
                "platId=" + getPlatId() +
                ", typePlat=" + getTypePlat().getLabel() +
                ", poids=" + getPoids() +
                ", nomPlat='" + nomPlat + '\'' +
                '}';
    }

    @Override
    public String getNomPlat() {
        return nomPlat;
    }
}
