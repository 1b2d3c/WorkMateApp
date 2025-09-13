package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.example.attendance.dto.Message;

public class MessageDAO {

    public boolean insertMessage(Message message) {
        String sql = "INSERT INTO messages (message_text, priority, start_datetime, end_datetime) VALUES (?, ?, ?, ?)";
        System.out.println("MessageDAO: insertMessage SQL: " + sql); // Debugging log
        System.out.println("MessageDAO: insertMessage Params: " + message.getMessageText() + ", " + message.getPriority() + ", " + message.getStartDatetime() + ", " + message.getEndDatetime()); // Debugging log
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message.getMessageText());
            pstmt.setString(2, message.getPriority());
            pstmt.setTimestamp(3, Timestamp.valueOf(message.getStartDatetime()));
            pstmt.setTimestamp(4, Timestamp.valueOf(message.getEndDatetime()));
            int affectedRows = pstmt.executeUpdate();
            System.out.println("MessageDAO: insertMessage affectedRows: " + affectedRows); // Debugging log
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Message getMessageById(int messageId) {
        String sql = "SELECT message_id, message_text, priority, start_datetime, end_datetime FROM messages WHERE message_id = ?";
        System.out.println("MessageDAO: getMessageById SQL: " + sql + ", ID: " + messageId); // Debugging log
        Message message = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    message = new Message(
                        rs.getInt("message_id"),
                        rs.getString("message_text"),
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

    public List<Message> getAllMessages() {
        List<Message> messageList = new ArrayList<>();
        String sql = "SELECT message_id, message_text, priority, start_datetime, end_datetime FROM messages ORDER BY start_datetime DESC";
        System.out.println("MessageDAO: getAllMessages SQL: " + sql); // Debugging log
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Message message = new Message(
                    rs.getInt("message_id"),
                    rs.getString("message_text"),
                    rs.getString("priority"),
                    rs.getTimestamp("start_datetime").toLocalDateTime(),
                    rs.getTimestamp("end_datetime").toLocalDateTime()
                );
                messageList.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("MessageDAO: getAllMessages retrieved " + messageList.size() + " messages."); // Debugging log
        return messageList;
    }

    public boolean updateMessage(Message message) {
        String sql = "UPDATE messages SET message_text = ?, priority = ?, start_datetime = ?, end_datetime = ? WHERE message_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, message.getMessageText());
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

    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM messages WHERE message_id = ?";
        try (Connection conn = DBConnection.getConnection();
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