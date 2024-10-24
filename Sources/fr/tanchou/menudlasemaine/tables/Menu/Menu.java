package fr.tanchou.menudlasemaine.tables.Menu;

import java.util.HashMap;
import java.util.Map;

public class Menu {
    private int menuId;
    private Map<String, Repas[]> repasParJour;

    public Menu(int menuId){
        this.menuId = menuId;
        this.repasParJour = new HashMap<>();

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (String jour : jours) {
            this.repasParJour.put(jour, new Repas[2]);
        }
    }

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public Map<String, Repas[]> getRepasParJour() {
        return repasParJour;
    }

    public void setRepasParJour(Map<String, Repas[]> repasParJour) {
        this.repasParJour = repasParJour;
    }
}
