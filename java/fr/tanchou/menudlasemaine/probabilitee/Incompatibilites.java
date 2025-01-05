package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.menu.Produits;

/**
 * Represents an incompatibility between two products: a vegetable and a starch.
 * <p>
 * This class is used to model combinations of products that should not be paired together.
 * </p>
 */
public class Incompatibilites {

    /** The vegetable involved in the incompatibility. */
    private final Produits legume;

    /** The starch involved in the incompatibility. */
    private final Produits feculent;

    /**
     * Constructs a new {@code Incompatibilites} instance with the specified vegetable and starch.
     *
     * @param legume   The vegetable that is incompatible with the starch.
     * @param feculent The starch that is incompatible with the vegetable.
     */
    public Incompatibilites(Produits legume, Produits feculent) {
        this.legume = legume;
        this.feculent = feculent;
    }

    /**
     * Returns a string representation of the incompatibility.
     *
     * @return A string describing the incompatibility between the vegetable and the starch.
     */
    @Override
    public String toString() {
        return legume + " et " + feculent + " ne vont pas ensemble.";
    }
}