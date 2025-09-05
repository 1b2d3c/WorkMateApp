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
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.User;

@WebServlet("/employee")
public class EmployeeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.jsp");
            return;
        }
        User user = (User) session.getAttribute("user");
        
        // 自身の勤怠履歴を取得
        List<Attendance> attendanceList = attendanceDAO.getAttendanceByUserId(user.getUserId());
        request.setAttribute("attendanceList", attendanceList);
        
        // 現在の打刻状態を取得
        Attendance latestAttendance = attendanceDAO.getLatestAttendanceByUserId(user.getUserId());
        if (latestAttendance != null && latestAttendance.getCheckOutTime() == null) {
            request.setAttribute("status", "出勤中");
        } else {
            request.setAttribute("status", "退勤済み");
        }
        
        // TODO: 自身に付与された連絡事項の表示ロジック
        // MessageDAO と MessageRoleDAO を使用して実装する必要があります。

        request.getRequestDispatcher("employee/dashboard.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.jsp");
            return;
        }
        User user = (User) session.getAttribute("user");
        
        String action = request.getParameter("action");

        if ("check_in".equals(action)) {
            // 出勤処理
            Attendance newAttendance = new Attendance(0, user.getUserId(), LocalDateTime.now(), null);
            attendanceDAO.insertAttendance(newAttendance);
        } else if ("check_out".equals(action)) {
            // 退勤処理
            Attendance latestAttendance = attendanceDAO.getLatestAttendanceByUserId(user.getUserId());
            if (latestAttendance != null && latestAttendance.getCheckOutTime() == null) {
                latestAttendance.setCheckOutTime(LocalDateTime.now());
                attendanceDAO.updateAttendance(latestAttendance);
            }
        }
        response.sendRedirect("employee");
    }
}
