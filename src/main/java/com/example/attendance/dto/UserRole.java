package com.example.attendance.dto;

/**
 * ユーザーと役割の関連付けを表現するDTOクラス。
 * `users_roles` テーブルに対応します。
 */
public class UserRole {
    private int userId;
    private int roleId;

    public UserRole() {}

    public UserRole(int userId, int roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
}
