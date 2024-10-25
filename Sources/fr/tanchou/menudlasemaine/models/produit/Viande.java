package fr.tanchou.menudlasemaine.models.produit;

import java.time.LocalDate;
import java.util.Objects;

public class Viande {
    private String nomViande;
    private int poids;
    private LocalDate lastUsed;

    public Viande(String nomViande, int poids, LocalDate lastUsed) {
        this.nomViande = nomViande;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Viande viande = (Viande) obj;
        return poids == viande.poids &&
                Objects.equals(nomViande, viande.nomViande) &&
                Objects.equals(lastUsed, viande.lastUsed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nomViande, poids, lastUsed);
    }

    @Override
    public String toString() {
        return "" + nomViande + " - poids :" + poids +" - dernière utilisation : " + lastUsed;
    }

    public String getNomViande() {
        return nomViande;
    }

    public void setNomViande(String nom) {
        this.nomViande = nom;
    }

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
