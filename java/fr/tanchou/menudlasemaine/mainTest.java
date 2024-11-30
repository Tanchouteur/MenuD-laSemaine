package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.menu.Menu;
import fr.tanchou.menudlasemaine.utils.generateur.MenuService;

public class mainTest {
    public static void main(String[] args) {

        Menu newMenu = MenuService.buildMenu();

        //MenuDAO.updateMenu(newMenu.getRepasParJour());

        System.out.println(newMenu);
    }
}
