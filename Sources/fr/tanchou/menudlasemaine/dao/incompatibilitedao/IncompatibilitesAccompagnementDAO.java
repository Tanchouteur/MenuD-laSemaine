package fr.tanchou.menudlasemaine.dao.incompatibilitedao;

import fr.tanchou.menudlasemaine.models.incompatibilite.IncompatibilitesAccompagnement;
import fr.tanchou.menudlasemaine.utils.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IncompatibilitesAccompagnementDAO {
    public void ajouterIncompatibilite(String legumeNom, String feculentNom) {
        String sql = "INSERT INTO Incompatibilites_Accompagnement (legume_nom, feculent_nom) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, legumeNom);
            pstmt.setString(2, feculentNom);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<IncompatibilitesAccompagnement> getIncompatibilites() {
        List<IncompatibilitesAccompagnement> incompatibilites = new ArrayList<>();
        String sql = "SELECT legume_nom, feculent_nom FROM Incompatibilites_Accompagnement";

        try (Connection conn = DatabaseConnection.getDataSource().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String legumeNom = rs.getString("legume_nom");
                String feculentNom = rs.getString("feculent_nom");
                incompatibilites.add(new IncompatibilitesAccompagnement(legumeNom, feculentNom));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return incompatibilites;
    }
}
