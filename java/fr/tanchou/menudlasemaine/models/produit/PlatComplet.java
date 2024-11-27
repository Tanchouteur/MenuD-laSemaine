package fr.tanchou.menudlasemaine.models.produit;

import fr.tanchou.menudlasemaine.enums.TypePlat;
import fr.tanchou.menudlasemaine.models.Plat;

import java.time.LocalDate;

public class PlatComplet extends Plat{
    public PlatComplet(String nomPlat, int poids, LocalDate lastUsed) {
        super(nomPlat,poids, lastUsed,TypePlat.COMPLET);
    }

    @Override
    public String toString() {
        return "PlatComplet{" +
                ", poids=" + getPoids() +
                ", nomPlat='" + getNom() + '\'' +
                '}';
    }

    @Override
    public String getNomPlat() {
        return getNom();
    }
}
