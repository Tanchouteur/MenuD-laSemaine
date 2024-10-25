package fr.tanchou.menudlasemaine.models;

import fr.tanchou.menudlasemaine.enums.TypePlat;

import java.time.LocalDate;

public class PlatComplet extends Plat {
    private final String nomPlat;
    private int poids;
    private LocalDate lastUsed;

    public PlatComplet(String nomPlat, int poids, LocalDate lastUsed) {
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

    @Override
    public int getPoids() {
        return poids;
    }

    @Override
    public void setPoids(int poids) {
        this.poids = poids;
    }

    public LocalDate getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(LocalDate lastUsed) {
        this.lastUsed = lastUsed;
    }
}
