package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.models.;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VuePoidsPlatDAO {
    private Connection connection;

    public VuePoidsPlatDAO(Connection connection) {
        this.connection = connection;
    }

    public List<VuePoidsPlat> getAll() throws SQLException {
        List<VuePoidsPlat> plats = new ArrayList<>();
        String sql = "SELECT * FROM VuePoidsPlats"; // Nom de la vue

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int platId = resultSet.getInt("plat_id");
                String nomPlat = resultSet.getString("nom_plat");
                double poidsFamille = resultSet.getDouble("poids_famille");
                double poidsSaison = resultSet.getDouble("poids_saison");
                double poidsMomentJournee = resultSet.getDouble("poids_moment_journee");
                double poidsMomentSemaine = resultSet.getDouble("poids_moment_semaine");

                VuePoidsPlat plat = new VuePoidsPlat(platId, nomPlat, poidsFamille, poidsSaison, poidsMomentJournee, poidsMomentSemaine);
                plats.add(plat);
            }
        }
        return plats;
    }
}
