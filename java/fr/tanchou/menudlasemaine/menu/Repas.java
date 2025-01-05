package fr.tanchou.menudlasemaine.menu;

/**
 * Represents a meal (Repas), which consists of an entree (starter) and a main dish (Plat).
 */
public record Repas(Produits entree, Plat plat) {

    /**
     * Returns a string representation of the meal, which includes the entree and the main dish.
     *
     * @return A string describing the meal with the entree and main dish.
     */
    @Override
    public String toString() {
        return "Repas { Entrée: " + entree + " - Plat: " + plat + " }";
    }
}