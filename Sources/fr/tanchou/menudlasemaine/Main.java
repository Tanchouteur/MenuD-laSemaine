package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.utils.MenuService;

public class Main {
    public static void main(String[] args) {

        MenuService.printTableMenu(MenuService.buildMenu());
    }
}
