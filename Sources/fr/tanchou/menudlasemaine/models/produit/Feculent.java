package fr.tanchou.menudlasemaine.models.produit;

import java.time.LocalDate;
import java.util.Objects;

public class Feculent {
    private String feculentName;
    private int poids;
    private LocalDate lastUsed;

    public Feculent(String feculentName, int poids, LocalDate lastUsed) {
        this.feculentName = feculentName;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Vérification de la référence
        if (obj == null || getClass() != obj.getClass()) return false; // Vérification du type
        Feculent feculent = (Feculent) obj; // Cast
        return poids == feculent.poids && // Vérification des attributs
                Objects.equals(feculentName, feculent.feculentName) &&
                Objects.equals(lastUsed, feculent.lastUsed);
    }

    // Redéfinition de hashCode
    @Override
    public int hashCode() {
        return Objects.hash(feculentName, poids, lastUsed); // Utilisation d'Objects.hash pour simplifier
    }

    @Override
    public String toString() {
        return "" + feculentName + " - poids :" + poids +" - dernière utilisation : " + lastUsed;
    }

    public String getFeculentName() {
        return feculentName;
    }

    public void setFeculentName(String feculentName) {
        this.feculentName = feculentName;
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
