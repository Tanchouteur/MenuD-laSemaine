package fr.tanchou.menudlasemaine.menu;

import fr.tanchou.menudlasemaine.dao.MenuDAO;

public class MenuManager {
    private Menu menu;

    public MenuManager() {
        menu = MenuDAO.getMenu();
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
        MenuDAO.updateMenu(menu.getListRepas());
    }
}
