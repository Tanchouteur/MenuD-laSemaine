package fr.tanchou.menudlasemaine.menu;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

import java.time.LocalDate;

/**
 * Represents a product (Produit) with its properties such as name, type, weight values for different moments and seasons,
 * and its last usage date.
 */
public class Produits {
    private int id;
    private final String nomProduit;
    private TypeProduit typeProduit;
    private LocalDate lastUsed;
    private final int poidsArbitraire;
    private int poidsLastUsed;
    private final int[] poidsMoment; // [midiSemaine, soirSemaine, midiWeekend, soirWeekend]
    private final int[] poidsSaison; // [printemps, été, automne, hiver]
    private int poidsFinal;

    /**
     * Constructs a new product with the specified properties.
     *
     * @param id            The product ID.
     * @param nomProduit    The name of the product.
     * @param poidsArbitraire The arbitrary weight of the product.
     * @param lastUsed      The last usage date of the product.
     * @param typeProduit   The type of the product (e.g., meat, vegetable).
     * @param poidsMoment   The weight values for different moments of the day (e.g., lunch on weekdays, dinner on weekends).
     * @param poidsSaison   The weight values for different seasons (e.g., spring, summer, autumn, winter).
     */
    public Produits(int id, String nomProduit, int poidsArbitraire, LocalDate lastUsed, TypeProduit typeProduit, int[] poidsMoment, int[] poidsSaison) {
        this.id = id;
        this.nomProduit = nomProduit;
        this.typeProduit = typeProduit;
        this.poidsArbitraire = poidsArbitraire;
        this.lastUsed = lastUsed;
        this.poidsMoment = poidsMoment;
        this.poidsSaison = poidsSaison;
    }

    /**
     * Constructs a new product with the specified properties, without the last usage date.
     *
     * @param nomProduit    The name of the product.
     * @param poidsArbitraire The arbitrary weight of the product.
     * @param typeProduit   The type of the product (e.g., meat, vegetable).
     * @param poidsMoment   The weight values for different moments of the day.
     * @param poidsSaison   The weight values for different seasons.
     */
    public Produits(String nomProduit, int poidsArbitraire, TypeProduit typeProduit, int[] poidsMoment, int[] poidsSaison) {
        this.nomProduit = nomProduit;
        this.typeProduit = typeProduit;
        this.poidsArbitraire = poidsArbitraire;
        this.poidsMoment = poidsMoment;
        this.poidsSaison = poidsSaison;
    }

    /**
     * Returns the string representation of the product, which is the product's name.
     *
     * @return The name of the product.
     */
    @Override
    public String toString() {
        return nomProduit;
    }

    /**
     * Retrieves the name of the product.
     *
     * @return The product's name.
     */
    public String getNomProduit() {
        return this.nomProduit;
    }

    /**
     * Retrieves the last used date of the product.
     *
     * @return The last used date of the product.
     */
    public LocalDate getLastUsed() {
        return lastUsed;
    }

    /**
     * Retrieves the type of the product.
     *
     * @return The type of the product.
     */
    public TypeProduit getType() {
        return this.typeProduit;
    }

    /**
     * Sets the type of the product.
     *
     * @param typeProduit The type of the product.
     */
    public void setType(TypeProduit typeProduit) {
        this.typeProduit = typeProduit;
    }

    /**
     * Retrieves the arbitrary weight of the product.
     *
     * @return The arbitrary weight of the product.
     */
    public int getPoidsArbitraire() {
        return poidsArbitraire;
    }

    /**
     * Retrieves the weight of the product based on its last usage.
     *
     * @return The weight of the product based on its last usage.
     */
    public int getPoidsLastUsed() {
        return poidsLastUsed;
    }

    /**
     * Sets the weight of the product based on its last usage.
     *
     * @param poidsLastUsed The weight based on last usage to set for the product.
     */
    public void setPoidsLastUsed(int poidsLastUsed) {
        this.poidsLastUsed = poidsLastUsed;
    }

    /**
     * Retrieves the weight values of the product for different moments of the day.
     *
     * @return The array representing the weight for different moments (e.g., lunch weekdays, dinner weekends).
     */
    public int[] getPoidsMoment() {
        return poidsMoment;
    }

    /**
     * Retrieves the weight values of the product for different seasons.
     *
     * @return The array representing the weight for different seasons (e.g., spring, summer, autumn, winter).
     */
    public int[] getPoidsSaison() {
        return poidsSaison;
    }

    /**
     * Retrieves the final weight of the product after all calculations.
     *
     * @return The final weight of the product.
     */
    public int getPoidsFinal() {
        return poidsFinal;
    }

    /**
     * Sets the final weight of the product after all calculations.
     *
     * @param poidsFinal The final weight to set for the product.
     */
    public void setPoidsFinal(int poidsFinal) {
        this.poidsFinal = poidsFinal;
    }

    /**
     * Retrieves the unique ID of the product.
     *
     * @return The unique ID of the product.
     */
    public int getId() {
        return id;
    }
}
