package fr.tanchou.menudlasemaine.menu;

/**
 * Represents a complete dish (PlatComplet), which is a dish made from a single product.
 * Extends the abstract class {@link Plat}.
 */
public class PlatComplet extends Plat {
    private final Produits plat;

    /**
     * Constructs a PlatComplet with the specified product.
     *
     * @param plat The product that forms the complete dish.
     */
    public PlatComplet(Produits plat) {
        this.plat = plat;
    }

    /**
     * Retrieves the product that forms the complete dish.
     *
     * @return The product (Plat) that represents the complete dish.
     */
    public Produits getPlat() {
        return plat;
    }

    /**
     * Retrieves the name of the complete dish.
     * This method returns the name of the product that represents the dish.
     *
     * @return The name of the dish as a string.
     */
    @Override
    public String getNomPlat() {
        return plat.getNomProduit();
    }
}