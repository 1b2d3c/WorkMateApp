package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import com.example.attendance.dto.Role;

/**
 * `roles`テーブルのデータアクセスオブジェクト（DAO）。
 * RoleオブジェクトのCRUD操作を管理します。
 */
public class RoleDAO {

    /**
     * 指定されたロール名がすでに存在するかどうかを確認します。
     * * @param rolename 確認するロール名
     * @return 存在する場合はtrue、それ以外はfalse
     * @throws SQLException データベースアクセスエラーが発生した場合
     */
    public boolean isRoleNameExists(String rolename) throws SQLException {
        String sql = "SELECT COUNT(*) FROM roles WHERE rolename = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rolename);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    /**
     * 新しい役割をデータベースに挿入します。
     *
     * @param role 挿入するRoleオブジェクト。
     * @return 新しく生成された役割ID。挿入に失敗した場合は-1を返します。
     * @throws SQLException データベースアクセスエラーが発生した場合。
     */
    public int insertRole(Role role) throws SQLException {
        String sql = "INSERT INTO roles (rolename, rolecategory) VALUES (?, ?::rolecategory)";
        int generatedId = -1;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, role.getRolename());
            PGobject roleCategoryObject = new PGobject();
            roleCategoryObject.setType("rolecategory");
            roleCategoryObject.setValue(role.getRolecategory());
            statement.setObject(2, roleCategoryObject);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
        }
        return generatedId;
    }

    /**
     * IDを使用して役割を取得します。
     *
     * @param roleId 取得する役割のID。
     * @return 見つかった場合はRoleオブジェクト、それ以外はnullを返します。
     * @throws SQLException データベースアクセスエラーが発生した場合。
     */
    public Role getRoleById(int roleId) throws SQLException {
        String sql = "SELECT role_id, rolename, rolecategory FROM roles WHERE role_id = ?";
        Role role = null;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    role = new Role(
                        rs.getInt("role_id"),
                        rs.getString("rolename"),
                        rs.getString("rolecategory")
                    );
                }
            }
        }
        return role;
    }

    /**
     * ロール名を使用して役割を取得します。
     *
     * @param rolename 取得する役割のロール名。
     * @return 見つかった場合はRoleオブジェクト、それ以外はnullを返します。
     * @throws SQLException データベースアクセスエラーが発生した場合。
     */
    public Role getRoleByRolename(String rolename) throws SQLException {
        String sql = "SELECT role_id, rolename, rolecategory FROM roles WHERE rolename = ?";
        Role role = null;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, rolename);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    role = new Role(
                        rs.getInt("role_id"),
                        rs.getString("rolename"),
                        rs.getString("rolecategory")
                    );
                }
            }
        }
        return role;
    }

    /**
     * データベースからすべての役割を取得します。
     *
     * @return すべてのRoleオブジェクトのリスト。
     * @throws SQLException データベースアクセスエラーが発生した場合。
     */
    public List<Role> getAllRoles() throws SQLException {
        String sql = "SELECT role_id, rolename, rolecategory FROM roles";
        List<Role> roles = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Role role = new Role(
                    rs.getInt("role_id"),
                    rs.getString("rolename"),
                    rs.getString("rolecategory")
                );
                roles.add(role);
            }
        }
        return roles;
    }

    /**
     * データベース内の既存の役割を更新します。
     *
     * @param role 更新されたデータを持つRoleオブジェクト。
     * @return 更新が成功した場合はtrue、それ以外はfalse。
     * @throws SQLException データベースアクセスエラーが発生した場合。
     */
    public boolean updateRole(Role role) throws SQLException {
        String sql = "UPDATE roles SET rolename = ?, rolecategory = ? WHERE role_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.getRolename());
            statement.setObject(2, pgEnum("rolecategory", role.getRolecategory()));
            statement.setInt(3, role.getRoleId());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * IDを使用してデータベースから役割を削除します。
     *
     * @param roleId 削除する役割のID。
     * @return 削除が成功した場合はtrue、それ以外はfalse。
     * @throws SQLException データベースアクセスエラーが発生した場合。
     */
    public boolean deleteRole(int roleId) throws SQLException {
        String sql = "DELETE FROM roles WHERE role_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    private static PGobject pgEnum(String enumType, String label) throws SQLException {
        PGobject o = new PGobject();
        o.setType(enumType);  // "role" または "user_role_type"
        o.setValue(label);    // DBのENUMラベルと完全一致
        return o;
    }
}