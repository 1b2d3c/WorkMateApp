package com.example.attendance.dto;

import java.time.LocalDateTime;

/**
 * 勤怠記録のデータを表現するDTOクラス。
 * `attendance` テーブルに対応します。
 */
public class Attendance {
    private int attendanceId;
    private int userId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public Attendance() {}

    public Attendance(int attendanceId, int userId, LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        this.attendanceId = attendanceId;
        this.userId = userId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    // Getters and Setters
    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
}

