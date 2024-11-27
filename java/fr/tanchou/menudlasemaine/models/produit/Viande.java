package fr.tanchou.menudlasemaine.models.produit;

import java.time.LocalDate;
import java.util.Objects;

public class Viande implements Poidsable {
    private String viandeNom;
    private int poids;
    private LocalDate lastUsed;

    public Viande(String viandeNom, int poids, LocalDate lastUsed) {
        this.viandeNom = viandeNom;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Viande viande = (Viande) obj;
        return poids == viande.poids &&
                Objects.equals(viandeNom, viande.viandeNom) &&
                Objects.equals(lastUsed, viande.lastUsed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(viandeNom, poids, lastUsed);
    }

    @Override
    public String toString() {
        return "" + viandeNom + " - poids :" + poids +" - dernière utilisation : " + lastUsed;
    }

    public String getViandeNom() {
        return viandeNom;
    }

    public void setViandeNom(String nom) {
        this.viandeNom = nom;
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

    public String getNom(){
        return viandeNom;
    }
}
