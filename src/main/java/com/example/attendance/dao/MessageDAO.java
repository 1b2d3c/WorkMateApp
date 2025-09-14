package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.postgresql.util.PGobject;

import com.example.attendance.dto.Message;

public class MessageDAO {

    public boolean insertMessage(Message message) {
        // priority は PostgreSQL の ENUM(priority) を想定。本文カラム名は message_text / message の両対応。
        final String sql1 = "INSERT INTO messages (message_text, priority, start_datetime, end_datetime) VALUES (?, ?, ?, ?)";
        final String sql2 = "INSERT INTO messages (message,      priority, start_datetime, end_datetime) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql1)) {

            pstmt.setString(1, message.getMessageText());
            pstmt.setObject(2, pgEnum("priority", message.getPriority()));
            pstmt.setTimestamp(3, Timestamp.valueOf(message.getStartDatetime()));
            if (message.getEndDatetime() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(message.getEndDatetime()));
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            // 42703: 未定義カラム（= DB に message_text が無く message の場合）
            if (!"42703".equals(e.getSQLState())) {
                e.printStackTrace();
                return false;
            }
            try (Connection conn2 = DBConnection.getConnection();
                 PreparedStatement pstmt2 = conn2.prepareStatement(sql2)) {

                pstmt2.setString(1, message.getMessageText());
                pstmt2.setObject(2, pgEnum("priority", message.getPriority()));
                pstmt2.setTimestamp(3, Timestamp.valueOf(message.getStartDatetime()));
                if (message.getEndDatetime() != null) {
                    pstmt2.setTimestamp(4, Timestamp.valueOf(message.getEndDatetime()));
                } else {
                    pstmt2.setNull(4, Types.TIMESTAMP);
                }
                return pstmt2.executeUpdate() > 0;

            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }

    public Message getMessageById(int messageId) {
        final String sql1 = "SELECT message_id, message_text, priority, start_datetime, end_datetime FROM messages WHERE message_id = ?";
        final String sql2 = "SELECT message_id, message AS message_text, priority, start_datetime, end_datetime FROM messages WHERE message_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql1)) {

            pstmt.setInt(1, messageId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            if (!"42703".equals(e.getSQLState())) {
                e.printStackTrace();
                return null;
            }
            try (Connection conn2 = DBConnection.getConnection();
                 PreparedStatement ps2 = conn2.prepareStatement(sql2)) {
                ps2.setInt(1, messageId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) {
                        return mapRow(rs2);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public List<Message> getAllMessages() {
        final List<Message> messageList = new ArrayList<>();
        final String sql1 = "SELECT message_id, message_text, priority, start_datetime, end_datetime FROM messages ORDER BY start_datetime DESC";
        final String sql2 = "SELECT message_id, message AS message_text, priority, start_datetime, end_datetime FROM messages ORDER BY start_datetime DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql1)) {

            while (rs.next()) {
                messageList.add(mapRow(rs));
            }
            return messageList;

        } catch (SQLException e) {
            if (!"42703".equals(e.getSQLState())) {
                e.printStackTrace();
                return messageList;
            }
            try (Connection conn2 = DBConnection.getConnection();
                 Statement stmt2 = conn2.createStatement();
                 ResultSet rs2 = stmt2.executeQuery(sql2)) {

                while (rs2.next()) {
                    messageList.add(mapRow(rs2));
                }
                return messageList;

            } catch (SQLException ex) {
                ex.printStackTrace();
                return messageList;
            }
        }
    }

    public boolean updateMessage(Message message) {
        final String sql1 = "UPDATE messages SET message_text = ?, priority = ?, start_datetime = ?, end_datetime = ? WHERE message_id = ?";
        final String sql2 = "UPDATE messages SET message      = ?, priority = ?, start_datetime = ?, end_datetime = ? WHERE message_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql1)) {

            pstmt.setString(1, message.getMessageText());
            pstmt.setObject(2, pgEnum("priority", message.getPriority()));
            pstmt.setTimestamp(3, Timestamp.valueOf(message.getStartDatetime()));
            if (message.getEndDatetime() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(message.getEndDatetime()));
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }
            pstmt.setInt(5, message.getMessageId());
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            if (!"42703".equals(e.getSQLState())) {
                e.printStackTrace();
                return false;
            }
            try (Connection conn2 = DBConnection.getConnection();
                 PreparedStatement ps2 = conn2.prepareStatement(sql2)) {

                ps2.setString(1, message.getMessageText());
                ps2.setObject(2, pgEnum("priority", message.getPriority()));
                ps2.setTimestamp(3, Timestamp.valueOf(message.getStartDatetime()));
                if (message.getEndDatetime() != null) {
                    ps2.setTimestamp(4, Timestamp.valueOf(message.getEndDatetime()));
                } else {
                    ps2.setNull(4, Types.TIMESTAMP);
                }
                ps2.setInt(5, message.getMessageId());
                return ps2.executeUpdate() > 0;

            } catch (SQLException ex) {
                ex.printStackTrace();
                return false;
            }
        }
    }

    public boolean deleteMessage(int messageId) {
        final String sql = "DELETE FROM messages WHERE message_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- helpers ---

    private static PGobject pgEnum(String enumType, String label) throws SQLException {
        PGobject o = new PGobject();
        o.setType(enumType);  // 例: "priority"
        o.setValue(label);    // DB 定義ラベルと完全一致（大文字小文字含む）
        return o;
    }

    private static Message mapRow(ResultSet rs) throws SQLException {
        Timestamp tsStart = rs.getTimestamp("start_datetime");
        Timestamp tsEnd = rs.getTimestamp("end_datetime");
        return new Message(
                rs.getInt("message_id"),
                rs.getString("message_text"),
                rs.getString("priority"),
                tsStart != null ? tsStart.toLocalDateTime() : null,
                tsEnd != null ? tsEnd.toLocalDateTime() : null
        );
    }
    
}