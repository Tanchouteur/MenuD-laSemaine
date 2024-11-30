package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.menu.Menu;
import fr.tanchou.menudlasemaine.utils.Factory;

public class mainTest {
    public static void main(String[] args) {
        Factory factory = new Factory();
        Menu newMenu = factory.buildMenu();

        //MenuDAO.updateMenu(newMenu.getRepasParJour());

        System.out.println(newMenu);
    }
}
