package fr.tanchou.menudlasemaine.dao;

import fr.tanchou.menudlasemaine.models.Feculent;
import fr.tanchou.menudlasemaine.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class FeculentDAO {

    // Ajoute un féculent avec un poids initial et une date d’utilisation nulle
    public void ajouterFeculent(Feculent feculent) {
        String sql = "INSERT INTO Feculent (nom_feculent, poids, last_used) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, feculent.getFeculentName());
            pstmt.setFloat(2, feculent.getPoids()); // poids initial
            pstmt.setDate(3, null); // dernière utilisation initiale
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupère un féculent par son nom
    public Feculent getFeculentByName(String feculentName) {
        String sql = "SELECT * FROM Feculent WHERE nom_feculent = ?";
        Feculent feculent = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feculentName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                feculent = new Feculent(rs.getString("nom_feculent"),
                        rs.getInt("poids"),
                        rs.getDate("last_used"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feculent;
    }

    // Récupère tous les féculents avec une priorité aux poids élevés
    public List<Feculent> getAllFeculents() {
        List<Feculent> feculents = new ArrayList<>();
        String sql = "SELECT * FROM Feculent ORDER BY poids DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                feculents.add(new Feculent(
                        rs.getString("nom_feculent"),
                        rs.getInt("poids"),
                        rs.getDate("last_used")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feculents;
    }

    // Sélectionne un féculent aléatoire avec une priorité au poids
    public Feculent getFeculentAleatoire() {
        String sql = "SELECT * FROM Feculent ORDER BY poids DESC LIMIT 1"; // Poids élevé priorisé
        Feculent feculent = null;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                feculent = new Feculent(rs.getString("nom_feculent"),
                        rs.getInt("poids"),
                        rs.getDate("last_used"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feculent;
    }

    // Met à jour le poids et la date d’utilisation après sélection d’un féculent
    public void updatePoidsEtLastUsed(Feculent feculent) {
        String sql = "UPDATE Feculent SET poids = poids - 1, last_used = ? WHERE nom_feculent = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, new java.sql.Date(new Date().getTime())); // Dernière utilisation actuelle
            pstmt.setString(2, feculent.getFeculentName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Supprime un féculent par son nom
    public void deleteFeculent(String feculentName) {
        String sql = "DELETE FROM Feculent WHERE nom_feculent = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, feculentName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
