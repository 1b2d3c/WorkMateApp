package com.example.attendance.dto;

/**
 * 役割のデータを表現するDTOクラス。
 * `roles` テーブルに対応します。
 */
public class Role {
    private int roleId;
    private String rolename;
    private String rolecategory; // DBのrolecategoryに対応

    public Role() {}

    public Role(int roleId, String rolename, String rolecategory) {
        this.roleId = roleId;
        this.rolename = rolename;
        this.rolecategory = rolecategory;
    }

    // Getters and Setters
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRolename() {
        return rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }

    public String getRolecategory() {
        return rolecategory;
    }

    public void setRolecategory(String rolecategory) {
        this.rolecategory = rolecategory;
    }
}

