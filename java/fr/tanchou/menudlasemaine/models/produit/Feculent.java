package fr.tanchou.menudlasemaine.models.produit;

import java.time.LocalDate;
import java.util.Objects;

public class Feculent implements Poidsable {
    private String feculentNom;
    private int poids;
    private LocalDate lastUsed;

    public Feculent(String feculentNom, int poids, LocalDate lastUsed) {
        this.feculentNom = feculentNom;
        this.poids = poids;
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // Vérification de la référence
        if (obj == null || getClass() != obj.getClass()) return false; // Vérification du type
        Feculent feculent = (Feculent) obj; // Cast
        return poids == feculent.poids && // Vérification des attributs
                Objects.equals(feculentNom, feculent.feculentNom) &&
                Objects.equals(lastUsed, feculent.lastUsed);
    }

    // Redéfinition de hashCode
    @Override
    public int hashCode() {
        return Objects.hash(feculentNom, poids, lastUsed); // Utilisation d'Objects.hash pour simplifier
    }

    @Override
    public String toString() {
        return "" + feculentNom + " - poids :" + poids +" - dernière utilisation : " + lastUsed;
    }

    public String getFeculentNom() {
        return feculentNom;
    }

    public void setFeculentNom(String feculentNom) {
        this.feculentNom = feculentNom;
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
