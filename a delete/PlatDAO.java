package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.utils.DatabaseConnection;
import fr.tanchou.menudlasemaine.models.Plat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class PlatDAO {

    protected Connection connection;

    public PlatDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    protected void insertPlat(Plat plat) throws SQLException {
        String sql = "INSERT INTO Plat (type_plat, poids) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, plat.getTypePlat().name());
            pstmt.setFloat(2, plat.getPoids());
            pstmt.executeUpdate();
            // Récupérer l'ID généré si nécessaire
        }
    }

    protected void updatePlat(Plat plat) throws SQLException {
        String sql = "UPDATE Plat SET type_plat = ?, poids = ? WHERE plat_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, plat.getTypePlat().name());
            pstmt.setFloat(2, plat.getPoids());
            pstmt.setInt(3, plat.getPlatId());
            pstmt.executeUpdate();
        }
    }
}
