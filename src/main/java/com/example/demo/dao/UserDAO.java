package com.example.demo.dao;

import com.example.demo.models.User;
import com.example.demo.utils.DBConnector;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("type"),
                        rs.getString("password")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User authenticate(String email, String password) {
        User user = findByEmail(email);
        if (user == null) {
            System.out.println("No user found for email: " + email);
            return null;
        }
        System.out.println("User found: " + user.getEmail() + ", stored hash: " + user.getPassword());
        boolean match = org.mindrot.jbcrypt.BCrypt.checkpw(password, user.getPassword());
        System.out.println("Password match: " + match);
        if (match) {
            return user;
        }
        return null;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO users (name, email, type, password) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getType());
            // Always hash the password before saving
            String hashed = org.mindrot.jbcrypt.BCrypt.hashpw(user.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt());
            System.out.println("[addUser] email: " + user.getEmail() + ", password: " + user.getPassword() + ", hash: " + hashed);
            stmt.setString(4, hashed);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateUser(User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            // Do not update password if not provided
            String sql = "UPDATE users SET name = ?, type = ? WHERE email = ?";
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getType());
                stmt.setString(3, user.getEmail());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            // Hash the new password before saving
            String sql = "UPDATE users SET name = ?, type = ?, password = ? WHERE email = ?";
            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getType());
                String hashed = org.mindrot.jbcrypt.BCrypt.hashpw(user.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt());
                System.out.println("[updateUser] email: " + user.getEmail() + ", new password: " + user.getPassword() + ", hash: " + hashed);
                stmt.setString(3, hashed);
                stmt.setString(4, user.getEmail());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean deleteUserByEmail(String email) {
        String sql = "DELETE FROM users WHERE email = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public java.util.List<User> getAllUsers() {
        java.util.List<User> users = new java.util.ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("type"),
                    rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
