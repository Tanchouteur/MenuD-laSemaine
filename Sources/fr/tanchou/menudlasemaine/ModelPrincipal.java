package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.dao.AccompagnementDAO;
import fr.tanchou.menudlasemaine.dao.FeculentDAO;
import fr.tanchou.menudlasemaine.dao.LegumeDAO;
import fr.tanchou.menudlasemaine.utils.AccompagnementGenerator;

public class ModelPrincipal {
    private LegumeDAO legumeDAO;
    private FeculentDAO feculentDAO;
    private AccompagnementDAO accompagnementDAO;

    public ModelPrincipal() {
        this.legumeDAO = new LegumeDAO();
        this.feculentDAO = new FeculentDAO();
        this.accompagnementDAO = new AccompagnementDAO();
    }

    public AccompagnementGenerator getAccompagnementGenerator() {
        return new AccompagnementGenerator(legumeDAO, feculentDAO, accompagnementDAO);
    }
}