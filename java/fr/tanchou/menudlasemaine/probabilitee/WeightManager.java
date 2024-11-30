package fr.tanchou.menudlasemaine.probabilitee;

import fr.tanchou.menudlasemaine.dao.ProduitDAO;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;

import java.util.*;

public class WeightManager {
    private Map<TypeProduit, LinkedList<Produits>> produitListMap;

    public WeightManager() {
        this.produitListMap = ProduitDAO.getAllProduits();
    }

    public LinkedList<Produits> getProduitsByType(TypeProduit typeProduit) {
        return produitListMap.get(typeProduit);
    }

}

