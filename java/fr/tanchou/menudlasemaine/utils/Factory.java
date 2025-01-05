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

/**
 * Factory class to generate menus, meals, and products for the API "Menu de la semaine".
 * <p>
 * This class uses weighted probabilities and compatibility rules
 * to create realistic menus based on the season, time of day, and day of the week.
 * </p>
 */
public class Factory {

    /** Manager for handling product weights and retrieving product lists. */
    private final WeightManager weightManager;

    /**
     * Constructs a new {@code Factory} instance and initializes the {@link WeightManager}.
     */
    public Factory() {
        this.weightManager = new WeightManager();
    }

    /**
     * Generates an entrée based on the time of day and season.
     *
     * @param momentInt  Integer representing the time of day.
     * @param saisonInt  Integer representing the season.
     * @return A {@link Produits} instance representing the entrée.
     */
    public Produits generateEntree(int momentInt, int saisonInt) {
        return WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.ENTREE), momentInt, saisonInt);
    }

    /**
     * Generates an accompaniment composed of a vegetable and/or a starch.
     * The combination depends on probabilities and compatibility rules.
     *
     * @param momentInt  Integer representing the time of day.
     * @param saisonInt  Integer representing the season.
     * @return An {@link Accompagnement} instance.
     */
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
                feculent = WeightsOperator.selectCompatibleProduct(legume, TypeProduit.FECULENT, momentInt, saisonInt);
            }else {
                feculent = WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.FECULENT), momentInt, saisonInt);
                assert feculent != null;
                legume = WeightsOperator.selectCompatibleProduct(feculent, TypeProduit.LEGUME, momentInt, saisonInt);
            }

            accompagnement = new Accompagnement(legume, feculent);
        }

        return accompagnement;
    }

    /**
     * Builds a main dish (plat) by choosing between a complete dish or a composed dish
     * (meat and accompaniment), based on their weighted probabilities.
     *
     * @param momentInt  Integer representing the time of day.
     * @param saisonInt  Integer representing the season.
     * @return A {@link Plat} instance representing the main dish.
     */
    public Plat buildPlat(int momentInt, int saisonInt) {
        Plat plat;
        Produits platComplet = WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.PLATCOMPLET), momentInt, saisonInt);

        Produits viande = WeightsOperator.selectBasedOnWeights(weightManager.getProduitsByType(TypeProduit.VIANDE), momentInt, saisonInt);
        Accompagnement accompagnement = generateAccompagnement(momentInt, saisonInt);

        int platComposeWeight;
        platComposeWeight = (accompagnement.getFeculent() == null) ? viande.getPoidsFinal() + accompagnement.getLegume().getPoidsFinal() : viande.getPoidsFinal() + accompagnement.getFeculent().getPoidsFinal();

        if (platComplet.getPoidsFinal() > (viande.getPoidsFinal() + platComposeWeight)) {
            plat = new PlatComplet(platComplet);
        } else {
            plat = new PlatCompose(viande, accompagnement);
        }

        return plat;
    }

    /**
     * Builds a meal (repas) composed of an optional entrée and a main dish.
     *
     * @param momentInt  Integer representing the time of day.
     * @param saisonInt  Integer representing the season.
     * @return A {@link Repas} instance.
     */
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

    /**
     * Builds a complete menu for the week, including lunch and dinner for each day.
     * The menu is adapted to the current season and differentiates between weekdays and weekends.
     *
     * @return A {@link Menu} instance representing the weekly menu.
     */
    public Menu buildMenu() {
        Month moisCourant = LocalDate.now().getMonth();
        int saisonInt = Saison.getSaisonIndexByMois(moisCourant.getValue());

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        String[] moments = {"midi", "soir"};

        Repas[][] listeRepas = new Repas[jours.length][moments.length];

        for (int i = 0; i < jours.length; i++) {
            for (int j = 0; j < moments.length; j++) {
                if (i < 6) {
                    listeRepas[i][j] = buildRepas(getMomentInt(MomentJournee.valueOf(moments[j].toUpperCase()), MomentSemaine.SEMAINE), saisonInt);
                }else {
                    listeRepas[i][j] = buildRepas(getMomentInt(MomentJournee.valueOf(moments[j].toUpperCase()), MomentSemaine.WEEKEND), saisonInt);
                }
            }
        }

        return new Menu(listeRepas);
    }

    /**
     * Converts the combination of {@link MomentJournee} and {@link MomentSemaine}
     * into an integer for internal use in weighting and product selection.
     *
     * @param momentJournee  The moment of the day (e.g., noon or evening).
     * @param momentSemaine  The moment of the week (e.g., weekday or weekend).
     * @return An integer representing the combined time context.
     */
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

    /**
     * Retrieves the {@link WeightManager} instance used for managing product weights.
     *
     * @return The {@link WeightManager} instance.
     */
    public WeightManager getWeightManager() {
        return weightManager;
    }
}
