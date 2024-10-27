package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.ViandeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.models.Accompagnement;
import fr.tanchou.menudlasemaine.models.Plat;
import fr.tanchou.menudlasemaine.models.PlatCompose;
import fr.tanchou.menudlasemaine.models.produit.Viande;
import fr.tanchou.menudlasemaine.probabilitee.LastUseWeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PlatComposeFactory {
    public static PlatCompose getRandomPlatCompose(MomentJournee momentJournee, MomentSemaine momentSemaine) {

        Viande selectedViande = ViandeFactory.getRandomViande(momentJournee, momentSemaine);

        Accompagnement randomAccompagnement = AccompagnementGenerator.generateAccompagnement(momentJournee, momentSemaine);

        return new PlatCompose(selectedViande.getPoids() + randomAccompagnement.getPoids() ,selectedViande, randomAccompagnement);
    }
}

