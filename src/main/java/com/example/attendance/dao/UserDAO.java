package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.example.attendance.dto.User;

/**
 * `users` テーブルへのデータベースアクセスを行うDAOクラス。
 */
public class UserDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/kintai_db";
    private static final String USER = "your_db_user";
    private static final String PASSWORD = "your_db_password";

    /**
     * 新しいユーザーをデータベースに挿入します。
     *
     * @param user 挿入するユーザーオブジェクト
     * @return 挿入が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean insertUser(User user) {
        String sql = "INSERT INTO users (username, password, role, enabled) VALUES (?, ?, ?::user_role_type, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setBoolean(4, user.isEnabled());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定されたIDのユーザーを検索します。
     *
     * @param userId 検索するユーザーのID
     * @return 見つかった場合はUserオブジェクト、見つからなかった場合はnull
     */
    public User getUserById(int userId) {
        String sql = "SELECT user_id, username, password, role, enabled FROM users WHERE user_id = ?";
        User user = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            e.printStackTrace();
        }
        return user;
    }

    /**
     * 指定されたユーザー名でユーザーを検索します。ログイン処理に使用します。
     *
     * @param username 検索するユーザー名
     * @return 見つかった場合はUserオブジェクト、見つからなかった場合はnull
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT user_id, username, password, role, enabled FROM users WHERE username = ?";
        User user = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
            e.printStackTrace();
        }
        return user;
    }

    /**
     * すべてのユーザーを取得します。
     *
     * @return すべてのユーザーのリスト
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT user_id, username, password, role, enabled FROM users";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
            e.printStackTrace();
        }
        return userList;
    }

    /**
     * ユーザー情報を更新します。
     *
     * @param user 更新するユーザーオブジェクト
     * @return 更新が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, role = ?::user_role_type, enabled = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setBoolean(4, user.isEnabled());
            pstmt.setInt(5, user.getUserId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定されたIDのユーザーを削除します。
     *
     * @param userId 削除するユーザーのID
     * @return 削除が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
