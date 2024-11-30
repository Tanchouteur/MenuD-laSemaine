package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.menu.Accompagnement;
import fr.tanchou.menudlasemaine.menu.PlatCompose;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;

public class PlatComposeFactory {
    public static PlatCompose getRandomPlatCompose(MomentJournee momentJournee, MomentSemaine momentSemaine, Saison saison, LastUseWeightManager lastUseWeightManager) {

        Viande selectedViande = ViandeFactory.getRandomViande(momentJournee, momentSemaine, lastUseWeightManager);

        Accompagnement randomAccompagnement = AccompagnementGenerator.generateAccompagnement(momentJournee, momentSemaine, saison, lastUseWeightManager);

        return new PlatCompose(selectedViande.getPoids() + randomAccompagnement.getPoids() ,selectedViande, randomAccompagnement);
    }
}

