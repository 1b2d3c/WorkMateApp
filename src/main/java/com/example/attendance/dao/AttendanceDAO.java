package com.example.attendance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.attendance.dto.Attendance;

public class AttendanceDAO {

	public boolean insertAttendance(Attendance attendance) {
		String sql = "INSERT INTO attendance (user_id, check_in_time, check_out_time) VALUES (?, ?, ?)";
		try (Connection conn = DBConnection.getConnection();
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
	
	/**
     * 指定されたユーザーIDと日付範囲の勤怠記録を取得します。
     *
     * @param userId 勤怠記録を取得するユーザーID
     * @param startDate 検索範囲の開始日時
     * @param endDate 検索範囲の終了日時
     * @return 該当する勤怠記録のリスト
     */
    public List<Attendance> getAttendanceByUserIdAndDateRange(int userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Attendance> attendanceList1 = new ArrayList<>();
        String sql = "SELECT * FROM attendance WHERE user_id = ? AND check_in_time BETWEEN ? AND ? ORDER BY check_in_time DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setObject(2, startDate);
            pstmt.setObject(3, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Attendance attendance = new Attendance(
                        rs.getInt("attendance_id"),
                        rs.getInt("user_id"),
                        rs.getObject("check_in_time", LocalDateTime.class),
                        rs.getObject("check_out_time", LocalDateTime.class)
                    );
                    attendanceList1.add(attendance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList1;
    }
    
    /**
     * 指定されたユーザーの月ごとの勤怠記録を取得します。
     *
     * @param userId ユーザーID
     * @param year 年
     * @param month 月
     * @return その月の勤怠記録のリスト
     */
    public List<Attendance> getMonthlyAttendance(int userId, int year, int month) {
        List<Attendance> attendanceList = new ArrayList<>();
        
        // 【修正箇所】BETWEEN句で日付範囲を指定するSQLに変更
        String sql = "SELECT * FROM attendance WHERE user_id = ? AND check_in_time BETWEEN ? AND ? ORDER BY check_in_time ASC";
        
        // 検索範囲の開始日と終了日を定義
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setObject(2, startDate);
            pstmt.setObject(3, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Attendance attendance = new Attendance(
                        rs.getInt("attendance_id"),
                        rs.getInt("user_id"),
                        rs.getObject("check_in_time", LocalDateTime.class),
                        rs.getObject("check_out_time", LocalDateTime.class)
                    );
                    attendanceList.add(attendance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

	public Attendance getAttendanceById(int attendanceId) {
		String sql = "SELECT attendance_id, user_id, check_in_time, check_out_time FROM attendance WHERE attendance_id = ?";
		Attendance attendance = null;
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attendanceId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					attendance = new Attendance(
							rs.getInt("attendance_id"),
							rs.getInt("user_id"),
							rs.getTimestamp("check_in_time").toLocalDateTime(),
							rs.getTimestamp("check_out_time") != null
									? rs.getTimestamp("check_out_time").toLocalDateTime()
									: null);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return attendance;
	}

	public List<Attendance> getAttendanceByUserId(int userId) {
		List<Attendance> attendanceList = new ArrayList<>();
		String sql = "SELECT attendance_id, user_id, check_in_time, check_out_time FROM attendance WHERE user_id = ? ORDER BY check_in_time DESC";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					Attendance attendance = new Attendance(
							rs.getInt("attendance_id"),
							rs.getInt("user_id"),
							rs.getTimestamp("check_in_time").toLocalDateTime(),
							rs.getTimestamp("check_out_time") != null
									? rs.getTimestamp("check_out_time").toLocalDateTime()
									: null);
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
		try (Connection conn = DBConnection.getConnection();
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

	public boolean deleteAttendance(int attendanceId) {
		String sql = "DELETE FROM attendance WHERE attendance_id = ?";
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, attendanceId);
			int affectedRows = pstmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Attendance getLatestAttendanceByUserId(int userId) {
		String sql = "SELECT * FROM attendance WHERE user_id = ? ORDER BY check_in_time DESC LIMIT 1";
		Attendance attendance = null;
		try (Connection conn = DBConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, userId);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					Timestamp checkOutTimestamp = rs.getTimestamp("check_out_time");
					LocalDateTime checkOutTime = (checkOutTimestamp != null) ? checkOutTimestamp.toLocalDateTime()
							: null;
					attendance = new Attendance(
							rs.getInt("attendance_id"),
							rs.getInt("user_id"),
							rs.getTimestamp("check_in_time").toLocalDateTime(),
							checkOutTime);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return attendance;
	}

	public List<Attendance> getAllAttendance() {
		List<Attendance> attendanceList = new ArrayList<>();
		String sql = "SELECT attendance_id, user_id, check_in_time, check_out_time FROM attendance ORDER BY check_in_time DESC";
		try (Connection conn = DBConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Attendance attendance = new Attendance(
						rs.getInt("attendance_id"),
						rs.getInt("user_id"),
						rs.getTimestamp("check_in_time").toLocalDateTime(),
						rs.getTimestamp("check_out_time") != null ? rs.getTimestamp("check_out_time").toLocalDateTime()
								: null);
				attendanceList.add(attendance);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return attendanceList;
	}
}