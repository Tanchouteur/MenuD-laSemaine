package fr.tanchou.menudlasemaine.controller;

import fr.tanchou.menudlasemaine.models.Menu;
import com.google.gson.Gson;
import fr.tanchou.menudlasemaine.utils.generateur.MenuService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/getMenu")
public class MenuController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        Menu menu = MenuService.buildMenu();

        Gson gson = new Gson();
        String menuJson = gson.toJson(menu);

        // Envoyer le JSON dans la réponse
        response.getWriter().write(menuJson);
    }
}
