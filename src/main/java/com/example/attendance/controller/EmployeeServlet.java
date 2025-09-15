package com.example.attendance.controller;

import java.io.IOException;
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
        
        // セッションから取得したユーザー名をリクエストスコープにセット
        request.setAttribute("username", user.getUsername());
        
        // 自身の勤怠履歴を取得
        List<Attendance> attendanceList = attendanceDAO.getAttendanceByUserId(user.getUserId());
        request.setAttribute("attendanceList", attendanceList);
        
        // 現在の打刻状態を取得
        Attendance latestAttendance = attendanceDAO.getLatestAttendanceByUserId(user.getUserId());
        String status;

        if (latestAttendance == null) {
            status = "退勤済み";
        } else if (latestAttendance.getCheckOutTime() != null) {
            status = "退勤済み";
        } else {
            status = "勤務中";
        }
        request.setAttribute("status", status.trim());
        
        // すべての連絡事項を取得して表示
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
		        // 降順ソート
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

        if ("check_in".equals(action)) {
            // 出勤処理
            Attendance newAttendance = new Attendance(user.getUserId(), LocalDateTime.now(), null);
            attendanceDAO.insertAttendance(newAttendance);
        } else if ("check_out".equals(action)) {
            // 退勤処理
            System.out.println("EmployeeServlet: Processing check_out action."); // Debugging log
            Attendance latestAttendance = attendanceDAO.getLatestAttendanceByUserId(user.getUserId());
            System.out.println("EmployeeServlet: latestAttendance for check_out: " + latestAttendance); // Debugging log

            if (latestAttendance != null && latestAttendance.getCheckOutTime() == null) {
                latestAttendance.setCheckOutTime(LocalDateTime.now());
                System.out.println("EmployeeServlet: Attempting to update attendanceId: " + latestAttendance.getAttendanceId() + " with checkOutTime: " + latestAttendance.getCheckOutTime()); // Debugging log
                boolean updateSuccess = attendanceDAO.updateAttendance(latestAttendance);
                System.out.println("EmployeeServlet: updateAttendance success: " + updateSuccess); // Debugging log
            } else {
                System.out.println("EmployeeServlet: Check-out condition not met. latestAttendance: " + latestAttendance + ", checkOutTime: " + (latestAttendance != null ? latestAttendance.getCheckOutTime() : "null")); // Debugging log
            }
        }
        response.sendRedirect(request.getContextPath() + "/employee");
    }
}