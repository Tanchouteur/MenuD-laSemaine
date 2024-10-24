package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.utils.AccompagnementGenerator;
import fr.tanchou.menudlasemaine.utils.PlatCompletFactory;

public class ModelPrincipal {

    public ModelPrincipal() {

    }

    public AccompagnementGenerator getAccompagnementGenerator() {
        return new AccompagnementGenerator();
    }

    public PlatCompletFactory getPlatCompletFactory() {
        return new PlatCompletFactory();
    }
}