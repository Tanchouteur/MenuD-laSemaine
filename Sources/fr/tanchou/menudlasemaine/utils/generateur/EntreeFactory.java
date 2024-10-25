package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.EntreeDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.models.produit.Entree;

import java.util.List;

public class EntreeFactory {
    public static Entree getRandomEntree(MomentJournee momentJournee) {
        List<Entree> entrees = new EntreeDAO().getAllEntrees();

        if (!entrees.isEmpty()) {
            int randomIndex = (int) (Math.random() * entrees.size());
            return entrees.get(randomIndex);
        } else {
            System.out.println("No entrees found");
            return null;
        }
    }
}