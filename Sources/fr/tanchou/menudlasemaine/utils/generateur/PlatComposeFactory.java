package fr.tanchou.menudlasemaine.utils.generateur;

import fr.tanchou.menudlasemaine.dao.produit.ViandeDAO;
import fr.tanchou.menudlasemaine.dao.weight.ProduitLastUseDAO;
import fr.tanchou.menudlasemaine.enums.MomentJournee;
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
    public static PlatCompose getRandomPlatCompose(MomentJournee momentJournee) {
        Random random = new Random();
        LastUseWeightManager lastUseWeightManager = new LastUseWeightManager(new ProduitLastUseDAO());

        Map<Viande, Integer> lastUseViandeWeights = lastUseWeightManager.calculateWeights(Viande.class, TypeProduit.VIANDE);
        List<Viande> viandes = ViandeDAO.getAllViandes();

        Viande selectedViande = WeightManager.selectBasedOnWeights(viandes, lastUseViandeWeights, random);

        Accompagnement randomAccompagnement = AccompagnementGenerator.generateAccompagnement(momentJournee);

        if (selectedViande != null) {
            ProduitLastUseDAO.updateLastUseDate(selectedViande.getViandeNom());
        }

        return new PlatCompose(1, selectedViande, randomAccompagnement);
    }
}

