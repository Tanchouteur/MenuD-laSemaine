package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;

public class PlatComplet extends Plat {
    private final String nomPlat;

    public PlatComplet(float poids, String nomPlat) {
        super(poids, TypePlat.COMPLET);
        this.nomPlat = nomPlat;
    }



    @Override
    public String toString() {
        return "PlatComplet{" +
                ", poids=" + getPoids() +
                ", nomPlat='" + nomPlat + '\'' +
                '}';
    }

    @Override
    public String getNomPlat() {
        return nomPlat;
    }
}
