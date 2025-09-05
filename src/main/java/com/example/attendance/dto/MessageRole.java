package com.example.attendance.dto;

/**
 * メッセージと役割の関連付けを表現するDTOクラス。
 * `messages_roles` テーブルに対応します。
 */
public class MessageRole {
    private int messageId;
    private int roleId;

    public MessageRole() {}

    public MessageRole(int messageId, int roleId) {
        this.messageId = messageId;
        this.roleId = roleId;
    }

    // Getters and Setters
    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
}
