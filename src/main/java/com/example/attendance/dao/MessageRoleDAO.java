package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.attendance.dto.MessageRole;

/**
 * `messages_roles` テーブルへのデータベースアクセスを行うDAOクラス。
 */
public class MessageRoleDAO {

    /**
     * 新しいメッセージと役割の関連付けをデータベースに挿入します。
     *
     * @param messageRole 挿入する関連付けオブジェクト
     * @return 挿入が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean addMessageRole(MessageRole messageRole) {
        String sql = "INSERT INTO messages_roles (message_id, role_id) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageRole.getMessageId());
            pstmt.setInt(2, messageRole.getRoleId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定されたメッセージIDに関連するすべての役割を取得します。
     *
     * @param messageId 検索するメッセージのID
     * @return メッセージに関連する役割のリスト
     */
    public List<MessageRole> getMessageRolesByMessageId(int messageId) {
        List<MessageRole> messageRoleList = new ArrayList<>();
        String sql = "SELECT message_id, role_id FROM messages_roles WHERE message_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MessageRole messageRole = new MessageRole(
                        rs.getInt("message_id"),
                        rs.getInt("role_id")
                    );
                    messageRoleList.add(messageRole);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messageRoleList;
    }

    /**
     * 指定されたメッセージIDと役割IDの関連付けを削除します。
     *
     * @param messageId 削除するメッセージのID
     * @param roleId 削除する役割のID
     * @return 削除が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean deleteMessageRole(int messageId, int roleId) {
        String sql = "DELETE FROM messages_roles WHERE message_id = ? AND role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, messageId);
            pstmt.setInt(2, roleId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
