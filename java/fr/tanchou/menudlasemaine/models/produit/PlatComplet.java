package fr.tanchou.menudlasemaine.models.produit;

import fr.tanchou.menudlasemaine.enums.TypePlat;
import fr.tanchou.menudlasemaine.models.Plat;

import java.time.LocalDate;
import java.util.Objects;

public class PlatComplet extends Plat{
    private final String nomPlat;
    private int poids;
    private LocalDate lastUsed;

    public PlatComplet(String nomPlat, int poids, LocalDate lastUsed) {
        super(poids, TypePlat.COMPLET);
        this.poids = poids;
        this.nomPlat = nomPlat;
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlatComplet platComplet = (PlatComplet) obj;
        return poids == platComplet.poids &&
                Objects.equals(nomPlat, platComplet.nomPlat) &&
                Objects.equals(lastUsed, platComplet.lastUsed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomPlat, poids, lastUsed);
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
        return this.poids;
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

    public String getNom(){
        return nomPlat;
    }
}
