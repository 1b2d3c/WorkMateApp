package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.attendance.dto.UserRole;

/**
 * `users_roles` テーブルへのデータベースアクセスを行うDAOクラス。
 */
public class UserRoleDAO {
	private static final String URL = "jdbc:postgresql://localhost:5432/workmate_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public boolean addUserRole(UserRole userRole) {
        String sql = "INSERT INTO users_roles (user_id, role_id) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userRole.getUserId());
            pstmt.setInt(2, userRole.getRoleId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<UserRole> getUserRolesByUserId(int userId) {
        List<UserRole> userRoleList = new ArrayList<>();
        String sql = "SELECT user_id, role_id FROM users_roles WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserRole userRole = new UserRole(
                        rs.getInt("user_id"),
                        rs.getInt("role_id")
                    );
                    userRoleList.add(userRole);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userRoleList;
    }

    public boolean deleteUserRole(int userId, int roleId) {
        String sql = "DELETE FROM users_roles WHERE user_id = ? AND role_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, roleId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
