package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;

import java.util.Date;

public class PlatComplet extends Plat {
    private final String nomPlat;
    private int poids;
    private Date lastUsed;

    public PlatComplet(String nomPlat, int poids, Date lastUsed) {
        super(poids, TypePlat.COMPLET);
        this.nomPlat = nomPlat;
        this.lastUsed = lastUsed;
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
