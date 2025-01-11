package fr.tanchou.menudlasemaine.menu;

import java.util.List;

/**
 * Represents an abstract dish (Plat) in a menu.
 * Subclasses must implement the method to provide the name of the dish.
 */
public abstract class Plat {

    /**
     * Abstract method to retrieve the name of the dish.
     *
     * @return A string representing the name of the dish.
     */
    public abstract String getNomPlat();

    public abstract List<Produits> getProductPlat();

    /**
     * Provides a string representation of the dish by returning its name.
     *
     * @return The name of the dish as a string.
     */
    @Override
    public String toString() {
        return getNomPlat();
    }
}