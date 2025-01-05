package fr.tanchou.menudlasemaine.menu;

public abstract class Plat {

    public abstract String getNomPlat(); // Méthode abstraite pour obtenir le nom du plat

    @Override
    public String toString() {
        return getNomPlat();
    }
}
