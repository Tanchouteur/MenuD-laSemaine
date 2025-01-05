package fr.tanchou.menudlasemaine.enums;

/**
 * Enum representing the four seasons of the year: Winter, Spring, Summer, and Autumn.
 * It also provides a method to determine the season index based on a given month.
 */
public enum Saison {

    /** Represents the Winter season. */
    HIVER,

    /** Represents the Spring season. */
    PRINTEMPS,

    /** Represents the Summer season. */
    ETE,

    /** Represents the Autumn season. */
    AUTOMNE;

    /**
     * Determines the season index for a given month.
     *
     * @param mois The month (1 for January, 12 for December).
     * @return The index of the season corresponding to the given month:
     *         - 0 for Spring (March to May),
     *         - 1 for Summer (June to August),
     *         - 2 for Autumn (September to November),
     *         - 3 for Winter (December to February).
     */
    public static int getSaisonIndexByMois(int mois) {
        if (mois >= 12 || mois <= 2) {
            return 3; // Winter
        } else if (mois <= 5) {
            return 0; // Spring
        } else if (mois <= 8) {
            return 1; // Summer
        } else {
            return 2; // Autumn
        }
    }
}