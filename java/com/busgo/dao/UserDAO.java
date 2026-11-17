package com.busgo.dao;

import com.busgo.db.DBConnection;
import com.busgo.model.User;

import java.sql.*;

/**
 * Data Access Object (DAO) for User entity using MySQL database.
 */
public class UserDAO {

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (full_name, mobile, email, age, gender, password_hash, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getMobile());
            stmt.setString(3, user.getEmail());
            stmt.setInt(4, user.getAge());
            stmt.setString(5, user.getGender());
            stmt.setString(6, user.getPasswordHash());
            stmt.setString(7, user.getRole());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("User registration error: " + e.getMessage());
            return false;
        }
    }

    public User authenticate(String identifier, String password) {
        String sql = "SELECT * FROM users WHERE (mobile = ? OR email = ? OR full_name = ?) AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, identifier);
            stmt.setString(2, identifier);
            stmt.setString(3, identifier);
            stmt.setString(4, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("mobile"),
                            rs.getString("email"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("password_hash"),
                            rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return null;
    }
}
