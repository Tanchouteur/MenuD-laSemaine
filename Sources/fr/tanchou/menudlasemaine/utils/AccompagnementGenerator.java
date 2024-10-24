package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.dao.FeculentDAO;
import fr.tanchou.menudlasemaine.dao.LegumeDAO;
import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.Feculent;
import fr.tanchou.menudlasemaine.models.Legume;

import java.util.List;
import java.util.Random;

public class AccompagnementGenerator {
    public static Accompagnement generateAccompagnement() {
        Random random = new Random();
        LegumeDAO legumeDAO = new LegumeDAO();
        FeculentDAO feculentDAO = new FeculentDAO();

        List<Legume> legumes = legumeDAO.getAllLegumes();
        List<Feculent> feculents = feculentDAO.getAllFeculents();

        Legume selectedLegume = legumes.get(random.nextInt(legumes.size()));
        Feculent selectedFeculent = feculents.get(random.nextInt(feculents.size()));

        int probaOneEmpty = random.nextInt(100);
        Accompagnement nouvelAccompagnement = null;

        if (probaOneEmpty > 70){
            if (random.nextBoolean()){//legume empty
                nouvelAccompagnement = new Accompagnement(selectedFeculent);
            }else {//feculent empty
                nouvelAccompagnement = new Accompagnement(selectedLegume);
            }
        }else {
            nouvelAccompagnement = new Accompagnement( selectedLegume, selectedFeculent);
        }

        return nouvelAccompagnement;
    }
}