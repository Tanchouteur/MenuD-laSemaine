package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.dao.AccompagnementDAO;
import fr.tanchou.menudlasemaine.dao.FeculentDAO;
import fr.tanchou.menudlasemaine.dao.LegumeDAO;
import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.Feculent;
import fr.tanchou.menudlasemaine.models.Legume;

import java.util.List;
import java.util.Random;

public class AccompagnementGenerator {
    private final Random random = new Random();
    private final LegumeDAO legumeDAO;
    private final FeculentDAO feculentDAO;
    private final AccompagnementDAO accompagnementDAO;

    public AccompagnementGenerator(LegumeDAO legumeDAO, FeculentDAO feculentDAO, AccompagnementDAO accompagnementDAO) {
        this.legumeDAO = legumeDAO;
        this.feculentDAO = feculentDAO;
        this.accompagnementDAO = accompagnementDAO;
    }

    public Accompagnement generateAndInsertAccompagnement() {
        List<Legume> legumes = legumeDAO.getAllLegumes();
        List<Feculent> feculents = feculentDAO.getAllFeculents();

        Legume selectedLegume = legumes.get(random.nextInt(legumes.size()));
        Feculent selectedFeculent = feculents.get(random.nextInt(feculents.size()));

        Accompagnement nouvelAccompagnement = new Accompagnement(0, selectedLegume, selectedFeculent);
        accompagnementDAO.insertAccompagnement(nouvelAccompagnement);

        return nouvelAccompagnement;
    }

    public Accompagnement generateAccompagnement() {
        List<Legume> legumes = legumeDAO.getAllLegumes();
        List<Feculent> feculents = feculentDAO.getAllFeculents();

        Legume selectedLegume = legumes.get(random.nextInt(legumes.size()));
        Feculent selectedFeculent = feculents.get(random.nextInt(feculents.size()));

        Accompagnement nouvelAccompagnement = new Accompagnement(0, selectedLegume, selectedFeculent);

        return nouvelAccompagnement;
    }
}