package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.dao.ProduitDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;

import java.util.*;

/**
 * Manages the retrieval and manipulation of product weights for menu generation.
 * <p>
 * This class acts as a bridge between DAOs and the weight-based selection mechanisms.
 * It allows accessing and filtering products based on their type.
 * </p>
 */
public class WeightManager {

    /** DAO for managing product-related operations. */
    private final ProduitDAO produitDAO;

    /** DAO for managing menu-related operations. */
    private final MenuDAO menuDAO;

    /**
     * Constructs a new {@code WeightManager} with initialized DAOs.
     * <p>
     * Initializes a {@code ProduitDAO} for accessing product data and a {@code MenuDAO}
     * for handling menu-related operations.
     * </p>
     */
    public WeightManager() {
        this.produitDAO = new ProduitDAO();
        this.menuDAO = new MenuDAO(produitDAO);
    }

    /**
     * Retrieves a list of products filtered by their type.
     *
     * @param typeProduit The type of products to retrieve (e.g., entrée, légume, viande).
     * @return A {@code LinkedList} containing products of the specified type.
     */
    public LinkedList<Produits> getProduitsByType(TypeProduit typeProduit) {
        return produitDAO.getProduitsByType(typeProduit);
    }

    /**
     * Returns the DAO responsible for product-related operations.
     *
     * @return The {@code ProduitDAO} instance.
     */
    public ProduitDAO getProduitDAO() {
        return produitDAO;
    }

    /**
     * Returns the DAO responsible for menu-related operations.
     *
     * @return The {@code MenuDAO} instance.
     */
    public MenuDAO getMenuDAO() {
        return menuDAO;
    }
}