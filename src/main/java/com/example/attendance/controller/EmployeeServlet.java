package com.example.attendance.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dao.MessageDAO;
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.Message;
import com.example.attendance.dto.User;

@WebServlet("/employee")
public class EmployeeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final AttendanceDAO attendanceDAO = new AttendanceDAO();
	private final MessageDAO messageDAO = new MessageDAO();

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    HttpSession session = request.getSession(false);
	    if (session == null || session.getAttribute("user") == null) {
	        response.sendRedirect(request.getContextPath() + "/login.jsp");
	        return;
	    }
	    
	    User user = (User) session.getAttribute("user");
	    request.setAttribute("username", user.getUsername());

	    boolean enabled = user.isEnabled();
	    request.setAttribute("enabled", enabled);

	    String status = "退勤済み";
	    if (enabled) {
	        Attendance currentAttendance = attendanceDAO.getLatestAttendanceByUserId(user.getUserId());
	        if (currentAttendance != null && currentAttendance.getCheckOutTime() == null) {
	            status = "勤務中";
	        }
	    }
	    request.setAttribute("status", status);
	    
	    List<Message> allMessages = messageDAO.getAllMessages();
	    LocalDateTime now = LocalDateTime.now();
	    List<Message> filteredMessages = allMessages.stream()
	            .filter(m -> !now.isBefore(m.getStartDatetime()) && !now.isAfter(m.getEndDatetime()))
	            .collect(Collectors.toList());
	    
	    Collections.sort(filteredMessages, new Comparator<Message>() {
	        @Override
	        public int compare(Message m1, Message m2) {
	            int p1 = getPriorityValue(m1.getPriority());
	            int p2 = getPriorityValue(m2.getPriority());
	            return Integer.compare(p2, p1);
	        }
	        private int getPriorityValue(String priority) {
	            switch (priority) {
	                case "high": return 3;
	                case "normal": return 2;
	                case "low": return 1;
	                default: return 0;
	            }
	        }
	    });
	    
	    request.setAttribute("messages", filteredMessages);

	    // 【修正箇所】勤怠履歴を取得
	    List<Attendance> attendanceList = attendanceDAO.getAttendanceByUserId(user.getUserId());
	    request.setAttribute("attendanceList", attendanceList);

	    // 【修正箇所】総労働時間の計算をアクションの条件分岐の外に移動
	    long totalMinutes = 0;
	    int totalCheckIns = 0; // 総出勤回数
	    for (Attendance attendance : attendanceList) {
	        if (attendance.getCheckInTime() != null) {
	            totalCheckIns++;
	            if (attendance.getCheckOutTime() != null) {
	                totalMinutes += Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
	            }
	        }
	    }
	    long totalHours = totalMinutes / 60;
	    long remainingMinutes = totalMinutes % 60;
	    request.setAttribute("totalWorkingTime", String.format("%d時間 %d分", totalHours, remainingMinutes));
	    request.setAttribute("totalCheckIns", totalCheckIns);

	    // 【修正箇所】月次レポートの計算ロジックも独立させる
	    String action = request.getParameter("action");
	    if ("monthly_summary".equals(action)) {
	        try {
	            int year = Integer.parseInt(request.getParameter("year"));
	            int month = Integer.parseInt(request.getParameter("month"));
	            
	            List<Attendance> monthlyAttendance = attendanceDAO.getMonthlyAttendance(user.getUserId(), year, month);

	            long monthlyTotalMinutes = 0;
	            for (Attendance attendance : monthlyAttendance) {
	                if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
	                    monthlyTotalMinutes += Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
	                }
	            }
	            
	            long monthlyTotalHours = monthlyTotalMinutes / 60;
	            long monthlyRemainingMinutes = monthlyTotalMinutes % 60;
	            
	            request.setAttribute("monthlyTotalTime", String.format("%d時間 %d分", monthlyTotalHours, monthlyRemainingMinutes));
	            request.setAttribute("checkInCount", monthlyAttendance.size());
	            request.setAttribute("selectedYear", year);
	            request.setAttribute("selectedMonth", month);
	        } catch (NumberFormatException e) {
	            // エラー時は何もしない
	        }
	    }
	    
	    System.out.println("総労働時間 (分): " + totalMinutes);
	    System.out.println("出勤回数: " + attendanceList.size());

	    String action1 = request.getParameter("action");
	    if ("monthly_summary".equals(action1)) {
	        try {
	            int year = Integer.parseInt(request.getParameter("year"));
	            int month = Integer.parseInt(request.getParameter("month"));
	            
	            List<Attendance> monthlyAttendance = attendanceDAO.getMonthlyAttendance(user.getUserId(), year, month);
	            
	            // 【デバッグ追加】
	            System.out.println(year + "年" + month + "月の勤怠記録数: " + monthlyAttendance.size());

	            // ... (既存の月次レポートロジック)

	        } catch (NumberFormatException e) {
	            // ...
	        }
	    }
	    
	    request.getRequestDispatcher("/jsp/employee.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("user") == null) {
			response.sendRedirect(request.getContextPath() + "/login.jsp");
			return;
		}

		User user = (User) session.getAttribute("user");
		String action = request.getParameter("action");
		
		// 無効なユーザーは勤怠打刻操作をスキップ
		if (!user.isEnabled()) {
			response.sendRedirect(request.getContextPath() + "/employee");
			return;
		}

		if ("check_in".equals(action)) {
			Attendance attendance = new Attendance(user.getUserId(), LocalDateTime.now(), null);
			attendanceDAO.insertAttendance(attendance);
		} else if ("check_out".equals(action)) {
			Attendance latestAttendance = attendanceDAO.getLatestAttendanceByUserId(user.getUserId());
			if (latestAttendance != null && latestAttendance.getCheckOutTime() == null) {
				latestAttendance.setCheckOutTime(LocalDateTime.now());
				attendanceDAO.updateAttendance(latestAttendance);
			}
		}
		response.sendRedirect(request.getContextPath() + "/employee");
	}
}