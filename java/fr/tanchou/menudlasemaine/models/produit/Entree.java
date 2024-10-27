package fr.tanchou.menudlasemaine.models.produit;

import java.time.LocalDate;
import java.util.Objects;

public class Entree implements Poidsable {
    private String nomEntree;
    private int poids;
    private LocalDate lastUsed;

    public Entree(String nomEntree, int poids, LocalDate lastUsed) {
        this.nomEntree = nomEntree;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Entree entree = (Entree) obj;
        return poids == entree.poids &&
                Objects.equals(nomEntree, entree.nomEntree) &&
                Objects.equals(lastUsed, entree.lastUsed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomEntree, poids, lastUsed);
    }

    @Override
    public String toString() {
        return "" + nomEntree + " - poids :" + poids +" - dernière utilisation : " + lastUsed;
    }

    public String getNomEntree() {
        return nomEntree;
    }

    public void setNomEntree(String nomEntree) {
        this.nomEntree = nomEntree;
    }

    @Override
    public int getPoids() {
        return poids;
    }

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
