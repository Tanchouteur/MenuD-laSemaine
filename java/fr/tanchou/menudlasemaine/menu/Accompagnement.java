package fr.tanchou.menudlasemaine.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an accompaniment, which is a combination of a vegetable and a starch.
 */
public record Accompagnement(Produits legume, Produits feculent) {

    /**
     * Constructs an Accompagnement with a specified vegetable and starch.
     *
     * @param legume   The vegetable component of the accompaniment. Can be {@code null}.
     * @param feculent The starch component of the accompaniment. Can be {@code null}.
     */
    public Accompagnement {
    }

    /**
     * Gets the vegetable component of the accompaniment.
     *
     * @return The vegetable, or {@code null} if none is present.
     */
    @Override
    public Produits legume() {
        return legume;
    }

    /**
     * Gets the starch component of the accompaniment.
     *
     * @return The starch, or {@code null} if none is present.
     */
    @Override
    public Produits feculent() {
        return feculent;
    }

    /**
     * Gets the name of the accompaniment as a concatenated string of its components.
     * <p>
     * If the vegetable is not {@code null}, its name will be included followed by a comma
     * if the starch is also present. If the starch is present, its name will be included.
     * </p>
     *
     * @return The name of the accompaniment, or an empty string if both components are {@code null}.
     */
    public String getNomAccompagnement() {
        String nomAccompagnement = "";
        if (legume != null) {
            nomAccompagnement += legume.getNomProduit() + ",";
        }
        if (feculent != null) {
            nomAccompagnement += feculent.getNomProduit();
        }
        return nomAccompagnement;
    }

    /**
     * Returns a string representation of the accompaniment.
     *
     * @return A string in the format {@code Accompagnement{legume=<legume>, feculent=<feculent>}}.
     */
    @Override
    public String toString() {
        return "Accompagnement{" +
                "legume=" + legume +
                ", feculent=" + feculent +
                '}';
    }

    public List<Produits> getProductUsed() {
        List<Produits> products = new ArrayList<>();
        if (legume != null) {
            products.add(legume);
        }
        if (feculent != null) {
            products.add(feculent);
        }
        return products;
    }
}