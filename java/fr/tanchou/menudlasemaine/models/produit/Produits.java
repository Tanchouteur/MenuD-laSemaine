package fr.tanchou.menudlasemaine.models.produit;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Produits {
    private final String nom;
    private int poids;
    private final LocalDate lastUsed;
    private final TypeProduit typeProduit;

    protected Produits(String nom, int poids, LocalDate lastUsed, TypeProduit typeProduit){
        this.nom = nom;
        this.poids = poids;
        this.lastUsed = lastUsed;
        this.typeProduit = typeProduit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Produits produits = (Produits) obj;
        return this.poids == produits.poids &&
                Objects.equals(nom, produits.nom) &&
                Objects.equals(lastUsed, produits.lastUsed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nom, poids, lastUsed);
    }

    @Override
    public String toString() {
        return nom + " - poids :" + poids;
    }

    public String getNom(){
        return this.nom;
    }

    public int getPoids() {
        return poids;
    }

    public LocalDate getLastUsed() {
        return lastUsed;
    }

    public void setPoids(int poids){
        this.poids = poids;
    }

    public TypeProduit getType() {
        return this.typeProduit;
    }
}
