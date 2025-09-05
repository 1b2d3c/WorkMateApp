package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.example.attendance.dto.Message;

/**
 * `messages` テーブルへのデータベースアクセスを行うDAOクラス。
 */
public class MessageDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/kintai_db";
    private static final String USER = "your_db_user";
    private static final String PASSWORD = "your_db_password";

    /**
     * 新しいメッセージをデータベースに挿入します。
     *
     * @param message 挿入するメッセージオブジェクト
     * @return 挿入が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean insertMessage(Message message) {
        String sql = "INSERT INTO messages (message, priority, start_datetime, end_datetime) VALUES (?, ?::priority, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message.getMessage());
            pstmt.setString(2, message.getPriority());
            pstmt.setTimestamp(3, Timestamp.valueOf(message.getStartDatetime()));
            pstmt.setTimestamp(4, Timestamp.valueOf(message.getEndDatetime()));
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定されたIDのメッセージを検索します。
     *
     * @param messageId 検索するメッセージのID
     * @return 見つかった場合はMessageオブジェクト、見つからなかった場合はnull
     */
    public Message getMessageById(int messageId) {
        String sql = "SELECT message_id, message, priority, start_datetime, end_datetime FROM messages WHERE message_id = ?";
        Message message = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    message = new Message(
                        rs.getInt("message_id"),
                        rs.getString("message"),
                        rs.getString("priority"),
                        rs.getTimestamp("start_datetime").toLocalDateTime(),
                        rs.getTimestamp("end_datetime").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return message;
    }

    /**
     * すべてのメッセージを取得します。
     *
     * @return すべてのメッセージのリスト
     */
    public List<Message> getAllMessages() {
        List<Message> messageList = new ArrayList<>();
        String sql = "SELECT message_id, message, priority, start_datetime, end_datetime FROM messages";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Message message = new Message(
                    rs.getInt("message_id"),
                    rs.getString("message"),
                    rs.getString("priority"),
                    rs.getTimestamp("start_datetime").toLocalDateTime(),
                    rs.getTimestamp("end_datetime").toLocalDateTime()
                );
                messageList.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messageList;
    }

    /**
     * メッセージを更新します。
     *
     * @param message 更新するメッセージオブジェクト
     * @return 更新が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean updateMessage(Message message) {
        String sql = "UPDATE messages SET message = ?, priority = ?::priority, start_datetime = ?, end_datetime = ? WHERE message_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message.getMessage());
            pstmt.setString(2, message.getPriority());
            pstmt.setTimestamp(3, Timestamp.valueOf(message.getStartDatetime()));
            pstmt.setTimestamp(4, Timestamp.valueOf(message.getEndDatetime()));
            pstmt.setInt(5, message.getMessageId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定されたIDのメッセージを削除します。
     *
     * @param messageId 削除するメッセージのID
     * @return 削除が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM messages WHERE message_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
