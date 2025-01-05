package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.dao.ProduitDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;

import java.util.*;

public class WeightManager {
    private final ProduitDAO produitDAO;
    private final MenuDAO menuDAO;

    public WeightManager() {
        this.produitDAO = new ProduitDAO();
        this.menuDAO = new MenuDAO(produitDAO);
    }

    public LinkedList<Produits> getProduitsByType(TypeProduit typeProduit) {
        return produitDAO.getProduitsByType(typeProduit);
    }

    public ProduitDAO getProduitDAO() {
        return produitDAO;
    }

    public MenuDAO getMenuDAO() {
        return menuDAO;
    }
}

