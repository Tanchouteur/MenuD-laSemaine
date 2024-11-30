package fr.tanchou.menudlasemaine.menu;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.time.LocalDate;

public class Produits {
    private final int id;
    private final String nomProduit;
    private final TypeProduit typeProduit;
    private final LocalDate lastUsed;
    private final int poidsArbitraire;
    private int poidsLastUsed;
    private final int[] poidsMoment; // [midiSemaine, soirSemaine, midiWeekend, soirWeekend]
    private final int[] poidsSaison; // [printemps, été, automne, hiver]
    private int poidsFinal;

    public Produits(int id,String nomProduit, int poidsArbitraire, LocalDate lastUsed, TypeProduit typeProduit, int[] poidsMoment, int[] poidsSaison) {
        this.id = id;
        this.nomProduit = nomProduit;
        this.poidsArbitraire = poidsArbitraire;
        this.lastUsed = lastUsed;
        this.typeProduit = typeProduit;
        this.poidsMoment = poidsMoment;
        this.poidsSaison = poidsSaison;
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

    public int[] getPoidsMoment() {
        return poidsMoment;
    }

    public int[] getPoidsSaison() {
        return poidsSaison;
    }

    public int getPoidsFinal() {
        return poidsFinal;
    }

    public void setPoidsFinal(int poidsFinal) {
        this.poidsFinal = poidsFinal;
    }
}
