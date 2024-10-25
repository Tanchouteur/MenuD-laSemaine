package fr.tanchou.menudlasemaine.models.produit;

import java.time.LocalDate;
import java.util.Objects;

public class Legume {
    private String legumeNom;
    private int poids;
    private LocalDate lastUsed;

    public Legume(String legumeNom, int poids, LocalDate lastUsed) {
        this.legumeNom = legumeNom;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Vérification de la référence
        if (obj == null || getClass() != obj.getClass()) return false; // Vérification du type
        Legume legume = (Legume) obj; // Cast
        return poids == legume.poids && // Vérification des attributs
                Objects.equals(legumeNom, legume.legumeNom) &&
                Objects.equals(lastUsed, legume.lastUsed);
    }

    // Redéfinition de hashCode
    @Override
    public int hashCode() {
        return Objects.hash(legumeNom, poids, lastUsed); // Utilisation d'Objects.hash pour simplifier
    }

    @Override
    public String toString() {
        return "" + legumeNom + " - poids :" + poids +" - dernière utilisation : " + lastUsed;
    }

    public String getLegumeNom() {
        return legumeNom;
    }
    public void setLegumeNom(String legumeNom) {
        this.legumeNom = legumeNom;
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
