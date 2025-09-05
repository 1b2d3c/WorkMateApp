package com.example.attendance.controller; 
 
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.attendance.dao.AttendanceDAO;
import com.example.attendance.dao.MessageDAO;
import com.example.attendance.dao.UserDAO;
import com.example.attendance.dto.Attendance;
import com.example.attendance.dto.Message;
import com.example.attendance.dto.User;

@WebServlet("/manager")
public class ManagerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final UserDAO userDAO = new UserDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MessageDAO messageDAO = new MessageDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "dashboard";
        }

        switch (action) {
            case "view_attendance":
                List<User> users = userDAO.getAllUsers();
                request.setAttribute("users", users);
                request.getRequestDispatcher("manager/dashboard.jsp?page=attendance").forward(request, response);
                break;
            case "view_users":
                List<User> allUsers = userDAO.getAllUsers();
                request.setAttribute("users", allUsers);
                request.getRequestDispatcher("manager/dashboard.jsp?page=users").forward(request, response);
                break;
            case "view_messages":
                List<Message> messages = messageDAO.getAllMessages();
                request.setAttribute("messages", messages);
                request.getRequestDispatcher("manager/dashboard.jsp?page=messages").forward(request, response);
                break;
            default:
                request.getRequestDispatcher("manager/dashboard.jsp").forward(request, response);
                break;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("manager");
            return;
        }

        switch (action) {
            case "add_attendance":
                // 勤怠記録の追加ロジック
                int userId = Integer.parseInt(request.getParameter("user_id"));
                LocalDateTime checkIn = LocalDateTime.parse(request.getParameter("check_in"));
                LocalDateTime checkOut = null;
                if (request.getParameter("check_out") != null && !request.getParameter("check_out").isEmpty()) {
                    checkOut = LocalDateTime.parse(request.getParameter("check_out"));
                }
                Attendance attendance = new Attendance(0, userId, checkIn, checkOut);
                attendanceDAO.insertAttendance(attendance);
                response.sendRedirect("manager?action=view_attendance");
                break;
            case "delete_attendance":
                // 勤怠記録の削除ロジック
                int attendanceId = Integer.parseInt(request.getParameter("attendance_id"));
                attendanceDAO.deleteAttendance(attendanceId);
                response.sendRedirect("manager?action=view_attendance");
                break;
            case "add_user":
                // ユーザー新規作成ロジック
                // パスワードは実際の環境ではハッシュ化が必要です
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String role = request.getParameter("role");
                boolean enabled = Boolean.parseBoolean(request.getParameter("enabled"));
                User newUser = new User(0, username, password, role, enabled);
                userDAO.insertUser(newUser);
                response.sendRedirect("manager?action=view_users");
                break;
            case "delete_user":
                // ユーザー削除ロジック
                int deleteUserId = Integer.parseInt(request.getParameter("user_id"));
                userDAO.deleteUser(deleteUserId);
                response.sendRedirect("manager?action=view_users");
                break;
            case "add_message":
                // 連絡/告知事項の追加ロジック
                String messageText = request.getParameter("message_text");
                String priority = request.getParameter("priority");
                LocalDateTime start = LocalDateTime.parse(request.getParameter("start_datetime"));
                LocalDateTime end = LocalDateTime.parse(request.getParameter("end_datetime"));
                Message newMessage = new Message(0, messageText, priority, start, end);
                messageDAO.insertMessage(newMessage);
                response.sendRedirect("manager?action=view_messages");
                break;
            case "edit_message":
                // 連絡/告知事項の編集ロジック
                int messageId = Integer.parseInt(request.getParameter("message_id"));
                String updatedMessageText = request.getParameter("message_text");
                String updatedPriority = request.getParameter("priority");
                LocalDateTime updatedStart = LocalDateTime.parse(request.getParameter("start_datetime"));
                LocalDateTime updatedEnd = LocalDateTime.parse(request.getParameter("end_datetime"));
                Message updatedMessage = new Message(messageId, updatedMessageText, updatedPriority, updatedStart, updatedEnd);
                messageDAO.updateMessage(updatedMessage);
                response.sendRedirect("manager?action=view_messages");
                break;
            default:
                response.sendRedirect("manager");
                break;
        }
    }
}
