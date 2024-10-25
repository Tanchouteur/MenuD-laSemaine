package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.enums.TypeProduit;

public class ProduitLimite {
    // Valeurs limites pour chaque type de produit
    private static final int VIANDE_POIDS_FAIBLE = 1;
    private static final int VIANDE_POIDS_LIMITE = 4;

    private static final int FECULENT_POIDS_FAIBLE = 1;
    private static final int FECULENT_POIDS_LIMITE = 2;

    private static final int LEGUME_POIDS_FAIBLE = 1;
    private static final int LEGUME_POIDS_NORMAL = 2;

    private static final int PLAT_COMPLET_POIDS_FAIBLE = 2;
    private static final int PLAT_COMPLET_POIDS_LIMITE = 5;

    private static final int ENTREE_POIDS_FAIBLE = 1;
    private static final int ENTREE_POIDS_LIMITE = 4;

    // Méthodes pour obtenir les poids
    public static int getPoidsFaible(TypeProduit typeProduit) {
        switch (typeProduit) {
            case VIANDE:
                return VIANDE_POIDS_FAIBLE;
            case FECULENT:
                return FECULENT_POIDS_FAIBLE;
            case LEGUME:
                return LEGUME_POIDS_FAIBLE;
            case PLAT_COMPLET:
                return PLAT_COMPLET_POIDS_FAIBLE;
            case ENTREE:
                return ENTREE_POIDS_FAIBLE;
            default:
                return 0; // ou une valeur par défaut
        }
    }

    public static int getViandePoidsLimite(TypeProduit typeProduit) {
        switch (typeProduit) {
            case VIANDE:
                return VIANDE_POIDS_LIMITE;
            case FECULENT:
                return FECULENT_POIDS_LIMITE;
            case LEGUME:
                return LEGUME_POIDS_NORMAL;
            case PLAT_COMPLET:
                return PLAT_COMPLET_POIDS_LIMITE;
            case ENTREE:
                return ENTREE_POIDS_LIMITE;
            default:
                return 0; // ou une valeur par défaut
        }
    }
}
