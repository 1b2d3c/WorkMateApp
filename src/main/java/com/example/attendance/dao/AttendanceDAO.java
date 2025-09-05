package com.example.attendance.dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.attendance.dto.Attendance;

/**
 * `attendance` テーブルへのデータベースアクセスを行うDAOクラス。
 */
public class AttendanceDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/kintai_db";
    private static final String USER = "your_db_user";
    private static final String PASSWORD = "your_db_password";

    public boolean insertAttendance(Attendance attendance) {
        String sql = "INSERT INTO attendance (user_id, check_in_time, check_out_time) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, attendance.getUserId());
            pstmt.setTimestamp(2, Timestamp.valueOf(attendance.getCheckInTime()));
            if (attendance.getCheckOutTime() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(attendance.getCheckOutTime()));
            } else {
                pstmt.setNull(3, Types.TIMESTAMP);
            }
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Attendance getAttendanceById(int attendanceId) {
        String sql = "SELECT attendance_id, user_id, check_in_time, check_out_time FROM attendance WHERE attendance_id = ?";
        Attendance attendance = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, attendanceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    attendance = new Attendance(
                        rs.getInt("attendance_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("check_in_time").toLocalDateTime(),
                        rs.getTimestamp("check_out_time") != null ? rs.getTimestamp("check_out_time").toLocalDateTime() : null
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendance;
    }

    public List<Attendance> getAttendanceByUserId(int userId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT attendance_id, user_id, check_in_time, check_out_time FROM attendance WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Attendance attendance = new Attendance(
                        rs.getInt("attendance_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("check_in_time").toLocalDateTime(),
                        rs.getTimestamp("check_out_time") != null ? rs.getTimestamp("check_out_time").toLocalDateTime() : null
                    );
                    attendanceList.add(attendance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    public boolean updateAttendance(Attendance attendance) {
        String sql = "UPDATE attendance SET check_out_time = ? WHERE attendance_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (attendance.getCheckOutTime() != null) {
                pstmt.setTimestamp(1, Timestamp.valueOf(attendance.getCheckOutTime()));
            } else {
                pstmt.setNull(1, Types.TIMESTAMP);
            }
            pstmt.setInt(2, attendance.getAttendanceId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 指定された勤怠IDの勤怠記録を削除します。
     *
     * @param attendanceId 削除する勤怠記録のID
     * @return 削除が成功した場合はtrue、失敗した場合はfalse
     */
    public boolean deleteAttendance(int attendanceId) {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, attendanceId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 指定されたユーザーIDの最新の勤怠記録を取得します。
     * 出勤状態の確認に使用します。
     *
     * @param userId 検索するユーザーのID
     * @return 最新のAttendanceオブジェクト、見つからない場合はnull
     */
    public Attendance getLatestAttendanceByUserId(int userId) {
        String sql = "SELECT * FROM attendance WHERE user_id = ? ORDER BY check_in_time DESC LIMIT 1";
        Attendance attendance = null;
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp checkOutTimestamp = rs.getTimestamp("check_out_time");
                    LocalDateTime checkOutTime = (checkOutTimestamp != null) ? checkOutTimestamp.toLocalDateTime() : null;
                    attendance = new Attendance(
                        rs.getInt("attendance_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("check_in_time").toLocalDateTime(),
                        checkOutTime
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendance;
    }
}
