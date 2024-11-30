package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.enums.Saison;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.*;
import fr.tanchou.menudlasemaine.probabilitee.WeightManager;
import fr.tanchou.menudlasemaine.probabilitee.WeightsOperator;

import java.time.LocalDate;
import java.time.Month;
import java.util.Random;

public class Factory {
    private final WeightManager weightManager;

    public Factory() {
        this.weightManager = new WeightManager();
    }

    // methode : generateEntree(): Entree
    public Produits generateEntree(int momentInt, int saisonInt) {
        return WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.ENTREE), momentInt, saisonInt);
    }

    // methode : generateAccompagnement(): Accompagnement
    public Accompagnement generateAccompagnement(int momentInt, int saisonInt) {
        Accompagnement accompagnement;

        Random random = new Random();
        int probaOneEmpty = random.nextInt(100);

        if (probaOneEmpty > 70) {
            if (random.nextBoolean()) { // Légume vide
                accompagnement = new Accompagnement(null, WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.FECULENT), momentInt, saisonInt));
            } else { // Féculent vide
                accompagnement = new Accompagnement(WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.LEGUME), momentInt, saisonInt), null);
            }
        } else {

            Produits feculent;
            Produits legume;

            if (random.nextBoolean()) {
                legume = WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.LEGUME), momentInt, saisonInt);
                assert legume != null;
                feculent = WeightsOperator.selectCompatibleProduct(legume, momentInt, saisonInt);
            }else {
                feculent = WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.FECULENT), momentInt, saisonInt);
                assert feculent != null;
                legume = WeightsOperator.selectCompatibleProduct(feculent, momentInt, saisonInt);
            }

            accompagnement = new Accompagnement(legume, feculent);
        }

        return accompagnement;
    }

    // methode : buildPlat(): Plat
    public Plat buildPlat(int momentInt, int saisonInt) {
        Plat plat;
        Produits platComplet = WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.PLATCOMPLET), momentInt, saisonInt);

        Produits viande = WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.VIANDE), momentInt, saisonInt);
        Accompagnement accompagnement = generateAccompagnement(momentInt, saisonInt);

        int platComposeWeight = viande.getPoidsFinal() + (accompagnement.getFeculent().getPoidsFinal() + accompagnement.getLegume().getPoidsFinal());

        if (platComplet.getPoidsFinal() > (viande.getPoidsFinal() + platComposeWeight)) {
            plat = new PlatComplet(platComplet);
        } else {
            plat = new PlatCompose(viande, accompagnement);
        }

        return plat;
    }

    // methode : buildRepas(): Repas
    public Repas buildRepas(int momentInt, int saisonInt) {
        Random random = new Random();

        int probaEntry = random.nextInt(100);
        Produits entree;

        if (probaEntry > 65){
            entree = generateEntree(momentInt, saisonInt);
        }else {
            entree = null;
        }

        return new Repas(entree, buildPlat(momentInt, saisonInt));
    }

    // methode : buildMenu(): Menu
    public Menu buildMenu() {
        Month moisCourant = LocalDate.now().getMonth();
        int saisonInt = Saison.getSaisonIndexByMois(moisCourant.getValue());

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        String[] moments = {"midi", "soir"};

        Repas[][] listeRepas = new Repas[jours.length][moments.length];

        for (int i = 0; i < jours.length; i++) {
            for (int j = 0; j < moments.length; j++) {
                if (i < 6) {
                    listeRepas[i][j] = buildRepas(getMomentInt(MomentJournee.valueOf(moments[j]), MomentSemaine.SEMAINE), saisonInt);
                }else {
                    listeRepas[i][j] = buildRepas(getMomentInt(MomentJournee.valueOf(moments[j]), MomentSemaine.WEEKEND), saisonInt);
                }
            }
        }

        return new Menu(listeRepas);
    }

    public int getMomentInt(MomentJournee momentJournee, MomentSemaine momentSemaine) {
        if (momentJournee == MomentJournee.MIDI) {
            if (momentSemaine == MomentSemaine.SEMAINE) {
                return 0;
            } else {
                return 2;
            }
        } else {
            if (momentSemaine == MomentSemaine.SEMAINE) {
                return 1;
            } else {
                return 3;
            }
        }
    }

    public WeightManager getWeightManager() {
        return weightManager;
    }
}
