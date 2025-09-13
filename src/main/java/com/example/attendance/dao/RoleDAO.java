package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.attendance.dto.Role;

/**
 * `roles` テーブルへのデータベースアクセスを行うDAOクラス。
 */
public class RoleDAO {

    public boolean insertRole(Role role) {
        String sql = "INSERT INTO roles (rolename, rolecategory) VALUES (?, ?::rolecategory)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getRolename());
            pstmt.setString(2, role.getRolecategory());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Role getRoleById(int roleId) {
        String sql = "SELECT role_id, rolename, rolecategory FROM roles WHERE role_id = ?";
        Role role = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    role = new Role(
                        rs.getInt("role_id"),
                        rs.getString("rolename"),
                        rs.getString("rolecategory")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return role;
    }

    public List<Role> getAllRoles() {
        List<Role> roleList = new ArrayList<>();
        String sql = "SELECT role_id, rolename, rolecategory FROM roles";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Role role = new Role(
                    rs.getInt("role_id"),
                    rs.getString("rolename"),
                    rs.getString("rolecategory")
                );
                roleList.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roleList;
    }

    public boolean updateRole(Role role) {
        String sql = "UPDATE roles SET rolename = ?, rolecategory = ?::rolecategory WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role.getRolename());
            pstmt.setString(2, role.getRolecategory());
            pstmt.setInt(3, role.getRoleId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRole(int roleId) {
        String sql = "DELETE FROM roles WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
