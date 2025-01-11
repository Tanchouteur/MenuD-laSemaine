package fr.tanchou.menudlasemaine.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a composed dish (PlatCompose), which consists of a meat product (viande)
 * and an accompaniment (Accompagnement).
 * Extends the abstract class {@link Plat}.
 */
public class PlatCompose extends Plat {
    private Produits viande;
    private Accompagnement accompagnement;

    /**
     * Constructs a PlatCompose with the specified meat and accompaniment.
     *
     * @param viande The meat product for the composed dish.
     * @param accompagnement The accompaniment for the composed dish.
     */
    public PlatCompose(Produits viande, Accompagnement accompagnement) {
        this.viande = viande;
        this.accompagnement = accompagnement;
    }

    /**
     * Retrieves the meat product for the composed dish.
     *
     * @return The meat product (viande) of the dish.
     */
    public Produits getViande() {
        return viande;
    }

    /**
     * Sets the meat product for the composed dish.
     *
     * @param viande The meat product to set.
     */
    public void setViande(Produits viande) {
        this.viande = viande;
    }

    /**
     * Retrieves the accompaniment for the composed dish.
     *
     * @return The accompaniment (Accompagnement) of the dish.
     */
    public Accompagnement getAccompagnement() {
        return accompagnement;
    }

    /**
     * Sets the accompaniment for the composed dish.
     *
     * @param accompagnement The accompaniment to set.
     */
    public void setAccompagnement(Accompagnement accompagnement) {
        this.accompagnement = accompagnement;
    }

    /**
     * Retrieves the name of the composed dish. The name is a concatenation of the meat product's name
     * and the accompaniment's name (if they are not null).
     *
     * @return The name of the composed dish as a string.
     */
    @Override
    public String getNomPlat() {
        return (viande != null ? viande.getNomProduit() : "") +
                (accompagnement != null ? "," + accompagnement.getNomAccompagnement() : "");
    }

    @Override
    public List<Produits> getProductPlat() {
        List<Produits> list = new ArrayList<>();
        list.add(viande);
        list.addAll(accompagnement.getProductUsed());
        return list;
    }

    /**
     * Returns the string representation of the composed dish, which is the name of the dish.
     *
     * @return The string representation of the composed dish.
     */
    @Override
    public String toString() {
        return getNomPlat();
    }
}