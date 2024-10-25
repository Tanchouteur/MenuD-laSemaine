package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.dao.ViandeDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.models.*;

import java.util.List;
import java.util.Random;

public class PlatComposeFactory{

    public static Plat getRandomPlatCompose(MomentJournee momentJournee) {
        List<Viande> viandes = new ViandeDAO().getAllViandes();
        Random random = new Random();

        Viande viande = viandes.get(random.nextInt(viandes.size()));

        Accompagnement randomAccompagnement = AccompagnementGenerator.generateAccompagnement(momentJournee);

        return new PlatCompose(1, viande, randomAccompagnement);
    }
}
