package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import com.example.attendance.dto.User;

public class UserDAO {

	public boolean insertUser(User user) {
        String sql1 = "INSERT INTO users (username, password, role, enabled) VALUES (?, ?, ?::user_role_type, ?)";
        String sql2 = "INSERT INTO users (username, password, role, is_enabled) VALUES (?, ?, ?::user_role_type, ?)";
        String sql3 = "INSERT INTO users (username, password, user_role, enabled) VALUES (?, ?, ?::user_role_type, ?)";
        String sql4 = "INSERT INTO users (username, password, user_role, is_enabled) VALUES (?, ?, ?::user_role_type, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            // role 列（text想定）は文字列で渡す
            pstmt.setString(3, user.getRole());
            pstmt.setBoolean(4, user.isEnabled());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // 42703 = 未定義カラム。enabled/is_enabled, role/user_role の揺れに対応
            if ("42703".equals(e.getSQLState())) {
                try (Connection conn2 = DBConnection.getConnection();
                     PreparedStatement ps2 = conn2.prepareStatement(sql2)) {
                    ps2.setString(1, user.getUsername());
                    ps2.setString(2, user.getPassword());
                    // role 列（text想定）は文字列で渡す
                    ps2.setString(3, user.getRole());
                    ps2.setBoolean(4, user.isEnabled());
                    return ps2.executeUpdate() > 0;
                } catch (SQLException e2) {
                    if ("42703".equals(e2.getSQLState())) {
                        try (Connection conn3 = DBConnection.getConnection();
                             PreparedStatement ps3 = conn3.prepareStatement(sql3)) {
                            ps3.setString(1, user.getUsername());
                            ps3.setString(2, user.getPassword());
                            // user_role 列（ENUM user_role_type）には PGobject で渡す
                            ps3.setObject(3, pgEnum("user_role_type", user.getRole()));
                            ps3.setBoolean(4, user.isEnabled());
                            return ps3.executeUpdate() > 0;
                        } catch (SQLException e3) {
                            if ("42703".equals(e3.getSQLState())) {
                                try (Connection conn4 = DBConnection.getConnection();
                                     PreparedStatement ps4 = conn4.prepareStatement(sql4)) {
                                    ps4.setString(1, user.getUsername());
                                    ps4.setString(2, user.getPassword());
                                    // user_role 列（ENUM user_role_type）には PGobject で渡す
                                    ps4.setObject(3, pgEnum("user_role_type", user.getRole()));
                                    ps4.setBoolean(4, user.isEnabled());
                                    return ps4.executeUpdate() > 0;
                                } catch (SQLException e4) {
                                    e4.printStackTrace();
                                    return false;
                                }
                            }
                            e3.printStackTrace();
                            return false;
                        }
                    }
                    e2.printStackTrace();
                    return false;
                }
            }
            e.printStackTrace();
            return false;
        }
    }

    public User getUserById(int userId) {
        String sql1 = "SELECT user_id, username, password, role, enabled FROM users WHERE user_id = ?";
        String sql2 = "SELECT user_id, username, password, role, is_enabled AS enabled FROM users WHERE user_id = ?";
        String sql3 = "SELECT user_id, username, password, user_role_type AS role, enabled FROM users WHERE user_id = ?";
        String sql4 = "SELECT user_id, username, password, user_role_type AS role, is_enabled AS enabled FROM users WHERE user_id = ?";
        User user = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("enabled")
                    );
                }
            }
        } catch (SQLException e) {
            if (!"42703".equals(e.getSQLState())) {
                e.printStackTrace();
                return null;
            }
            // 別名にフォールバック
            user = fetchUserWithFallback(sql2, userId);
            if (user == null) user = fetchUserWithFallback(sql3, userId);
            if (user == null) user = fetchUserWithFallback(sql4, userId);
        }
        return user;
    }

    public User getUserByUsername(String username) {
        String sql1 = "SELECT user_id, username, password, role, enabled FROM users WHERE username = ?";
        String sql2 = "SELECT user_id, username, password, role, is_enabled AS enabled FROM users WHERE username = ?";
        String sql3 = "SELECT user_id, username, password, user_role_type AS role, enabled FROM users WHERE username = ?";
        String sql4 = "SELECT user_id, username, password, user_role_type AS role, is_enabled AS enabled FROM users WHERE username = ?";
        User user = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("enabled")
                    );
                }
            }
        } catch (SQLException e) {
            if (!"42703".equals(e.getSQLState())) {
                e.printStackTrace();
                return null;
            }
            user = fetchUserWithFallback(sql2, username);
            if (user == null) user = fetchUserWithFallback(sql3, username);
            if (user == null) user = fetchUserWithFallback(sql4, username);
        }
        return user;
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql1 = "SELECT user_id, username, password, role, enabled FROM users";
        String sql2 = "SELECT user_id, username, password, role, is_enabled AS enabled FROM users";
        String sql3 = "SELECT user_id, username, password, user_role_type AS role, enabled FROM users";
        String sql4 = "SELECT user_id, username, password, user_role_type AS role, is_enabled AS enabled FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql1)) {
            while (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getBoolean("enabled")
                );
                userList.add(user);
            }
        } catch (SQLException e) {
            if (!"42703".equals(e.getSQLState())) {
                e.printStackTrace();
                return userList;
            }
            // 別名にフォールバック（最小限）
            userList = fetchUsersWithFallback(sql2);
            if (userList.isEmpty()) userList = fetchUsersWithFallback(sql3);
            if (userList.isEmpty()) userList = fetchUsersWithFallback(sql4);
        }
        return userList;
    }

    public boolean updateUser(User user) {
        String sql1 = "UPDATE users SET username = ?, password = ?, role = ?, enabled = ? WHERE user_id = ?";
        String sql2 = "UPDATE users SET username = ?, password = ?, role = ?, is_enabled = ? WHERE user_id = ?";
        String sql3 = "UPDATE users SET username = ?, password = ?, user_role = ?, enabled = ? WHERE user_id = ?";
        String sql4 = "UPDATE users SET username = ?, password = ?, user_role = ?, is_enabled = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql1)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            // role 列（text想定）は文字列で渡す
            pstmt.setString(3, user.getRole());
            pstmt.setBoolean(4, user.isEnabled());
            pstmt.setInt(5, user.getUserId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            if ("42703".equals(e.getSQLState())) {
                // role + is_enabled
                try (Connection conn2 = DBConnection.getConnection();
                     PreparedStatement ps2 = conn2.prepareStatement(sql2)) {
                    ps2.setString(1, user.getUsername());
                    ps2.setString(2, user.getPassword());
                    // role 列（text想定）は文字列で渡す
                    ps2.setString(3, user.getRole());
                    ps2.setBoolean(4, user.isEnabled());
                    ps2.setInt(5, user.getUserId());
                    return ps2.executeUpdate() > 0;
                } catch (SQLException e2) {
                    if ("42703".equals(e2.getSQLState())) {
                        // user_role + enabled
                        try (Connection conn3 = DBConnection.getConnection();
                             PreparedStatement ps3 = conn3.prepareStatement(sql3)) {
                            ps3.setString(1, user.getUsername());
                            ps3.setString(2, user.getPassword());
                            // user_role 列（ENUM user_role_type）には PGobject で渡す
                            ps3.setObject(3, pgEnum("user_role_type", user.getRole()));
                            ps3.setBoolean(4, user.isEnabled());
                            ps3.setInt(5, user.getUserId());
                            return ps3.executeUpdate() > 0;
                        } catch (SQLException e3) {
                            if ("42703".equals(e3.getSQLState())) {
                                // user_role + is_enabled
                                try (Connection conn4 = DBConnection.getConnection();
                                     PreparedStatement ps4 = conn4.prepareStatement(sql4)) {
                                    ps4.setString(1, user.getUsername());
                                    ps4.setString(2, user.getPassword());
                                    // user_role 列（ENUM user_role_type）には PGobject で渡す
                                    ps4.setObject(3, pgEnum("user_role_type", user.getRole()));
                                    ps4.setBoolean(4, user.isEnabled());
                                    ps4.setInt(5, user.getUserId());
                                    return ps4.executeUpdate() > 0;
                                } catch (SQLException e4) {
                                    e4.printStackTrace();
                                    return false;
                                }
                            }
                            e3.printStackTrace();
                            return false;
                        }
                    }
                    e2.printStackTrace();
                    return false;
                }
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateUserEnabled(int userId, boolean enabled) {
        String sql = "UPDATE users SET enabled = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, enabled);
            pstmt.setInt(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // --- helpers: フォールバック取得（簡潔版） ---
    private User fetchUserWithFallback(String sql, int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("enabled")
                    );
                }
            }
        } catch (SQLException ignore) {}
        return null;
    }

    private User fetchUserWithFallback(String sql, String username) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getBoolean("enabled")
                    );
                }
            }
        } catch (SQLException ignore) {}
        return null;
    }

    private List<User> fetchUsersWithFallback(String sql) {
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getBoolean("enabled")
                ));
            }
        } catch (SQLException ignore) {}
        return list;
    }
    
    public List<User> getUsersByRoleId(int roleId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.* FROM users u JOIN users_roles ur ON u.user_id = ur.user_id WHERE ur.role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRole(rs.getString("role")); // または必要に応じて `user_role_type` を処理
                    user.setEnabled(rs.getBoolean("enabled"));
                    users.add(user);
                }
            }
        }
        return users;
    }

    private static PGobject pgEnum(String enumType, String label) throws SQLException {
        PGobject o = new PGobject();
        o.setType(enumType);  // "role" または "user_role_type"
        o.setValue(label);    // DBのENUMラベルと完全一致
        return o;
    }
}