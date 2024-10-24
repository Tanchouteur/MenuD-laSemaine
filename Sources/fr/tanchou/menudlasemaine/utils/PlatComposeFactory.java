package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.dao.ViandeDAO;
import fr.tanchou.menudlasemaine.models.*;

import java.util.List;
import java.util.Random;

public class PlatComposeFactory{

    public static Plat getRandomPlatCompose() {
        List<Viande> viandes = new ViandeDAO().getAllViandes();
        Random random = new Random();

        Viande viande = viandes.get(random.nextInt(viandes.size()));

        Accompagnement randomAccompagnement = AccompagnementGenerator.generateAccompagnement();

        return new PlatCompose(1, viande, randomAccompagnement);
    }
}
