package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.dao.MenuDAO;
import fr.tanchou.menudlasemaine.dao.ProduitDAO;
import fr.tanchou.menudlasemaine.menu.Menu;
import fr.tanchou.menudlasemaine.utils.Factory;

public class mainTest {
    public static void main(String[] args) {
        Factory factory = new Factory();
        Menu newMenu = factory.buildMenu();
        ProduitDAO produitDAO = new ProduitDAO();
        MenuDAO menuDAO = new MenuDAO(produitDAO);
        menuDAO.updateMenu(newMenu.getListRepas());

        System.out.println(newMenu);
    }
}
