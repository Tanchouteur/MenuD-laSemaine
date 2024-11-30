package fr.tanchou.menudlasemaine.menu;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.time.LocalDate;

public class Produits {
    private final String nomProduit;
    private final TypeProduit typeProduit;
    private final LocalDate lastUsed;
    private final int poidsArbitraire;
    private int poidsLastUsed;
    private int poidsMoment;
    private int poidsSaison;
    private int poidsFinal;

    protected Produits(String nomProduit, int poidsArbitraire, LocalDate lastUsed, TypeProduit typeProduit){
        this.nomProduit = nomProduit;
        this.poidsArbitraire = poidsArbitraire;
        this.lastUsed = lastUsed;
        this.typeProduit = typeProduit;
    }

    @Override
    public String toString() {
        return nomProduit;
    }

    public String getNomProduit(){
        return this.nomProduit;
    }

    public LocalDate getLastUsed() {
        return lastUsed;
    }

    public TypeProduit getType() {
        return this.typeProduit;
    }

    public int getPoidsArbitraire() {
        return poidsArbitraire;
    }

    public int getPoidsLastUsed() {
        return poidsLastUsed;
    }

    public void setPoidsLastUsed(int poidsLastUsed) {
        this.poidsLastUsed = poidsLastUsed;
    }

    public int getPoidsMoment() {
        return poidsMoment;
    }

    public void setPoidsMoment(int poidsMoment) {
        this.poidsMoment = poidsMoment;
    }

    public int getPoidsSaison() {
        return poidsSaison;
    }

    public void setPoidsSaison(int poidsSaison) {
        this.poidsSaison = poidsSaison;
    }

    public int getPoidsFinal() {
        return poidsFinal;
    }

    public void setPoidsFinal(int poidsFinal) {
        this.poidsFinal = poidsFinal;
    }
}
