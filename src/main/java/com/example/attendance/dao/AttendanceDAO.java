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
	private static final String URL = "jdbc:postgresql://localhost:5432/workmate_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

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