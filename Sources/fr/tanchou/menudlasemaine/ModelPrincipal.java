package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.dao.AccompagnementDAO;
import fr.tanchou.menudlasemaine.dao.FeculentDAO;
import fr.tanchou.menudlasemaine.dao.LegumeDAO;
import fr.tanchou.menudlasemaine.dao.PlatCompletDAO;
import fr.tanchou.menudlasemaine.utils.AccompagnementGenerator;
import fr.tanchou.menudlasemaine.utils.PlatCompletFactory;

public class ModelPrincipal {
    private final LegumeDAO legumeDAO;
    private final FeculentDAO feculentDAO;
    private final AccompagnementDAO accompagnementDAO;
    private final PlatCompletDAO platCompletDAO;

    public ModelPrincipal() {
        this.legumeDAO = new LegumeDAO();
        this.feculentDAO = new FeculentDAO();
        this.accompagnementDAO = new AccompagnementDAO();
        this.platCompletDAO = new PlatCompletDAO();
    }

    public AccompagnementGenerator getAccompagnementGenerator() {
        return new AccompagnementGenerator(legumeDAO, feculentDAO, accompagnementDAO);
    }

    public PlatCompletFactory getPlatCompletFactory() {
        return new PlatCompletFactory(platCompletDAO);
    }
}