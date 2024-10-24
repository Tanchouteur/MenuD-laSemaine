package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.dao.EntreeDAO;
import fr.tanchou.menudlasemaine.models.Entree;

import java.util.List;

public class EntreeFactory {
    public static Entree getRandomEntree() {
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
