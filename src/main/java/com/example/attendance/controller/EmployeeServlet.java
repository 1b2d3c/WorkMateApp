package com.example.attendance.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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
        List<Message> messages = messageDAO.getAllMessages();
        request.setAttribute("messages", messages);

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